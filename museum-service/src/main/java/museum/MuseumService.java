package museum;

import com.google.common.collect.Maps;
import com.mongodb.async.client.MongoClient;
import com.mongodb.async.client.MongoClients;
import io.javalin.Javalin;
import io.netty.channel.Channel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import museum.boosters.BoosterType;
import museum.configuration.ConfigurationManager;
import museum.data.BoosterInfo;
import museum.data.Unique;
import museum.data.UserInfo;
import museum.donate.BoosterService;
import museum.donate.DonateService;
import museum.donate.IBoosterService;
import museum.donate.IDonateService;
import museum.handlers.PackageHandler;
import museum.packages.*;
import museum.realm.RealmsController;
import museum.service.user.IUserService;
import museum.service.user.UserService;
import museum.socket.ServerSocket;
import museum.socket.ServerSocketHandler;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import ru.cristalix.core.CoreApi;
import ru.cristalix.core.GlobalSerializers;
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

@Getter
@RequiredArgsConstructor
public class MuseumService {

	@Getter
	private static MuseumService instance;

	public static final long DEFAULT_BOOSTER_TIME = TimeUnit.HOURS.toMillis(1L);
	public static final int INCOME_MULTIPLIER = 10;
	private final String password;
	private final String databaseName;
	private final String databaseUrl;

	@SuppressWarnings ("rawtypes")
	private final Map<Class<? extends MuseumPackage>, PackageHandler> handlerMap = new HashMap<>();

	//	private static final Map<DonateType, BiPredicate<UserTransactionPackage, UserInfo>> TRANSACTION_PRE_AUTHORIZE_MAP = new HashMap<DonateType, BiPredicate<UserTransactionPackage, UserInfo>>() {{
	//		put(DonateType.GLOBAL_MONEY_BOOSTER, globalBoosterPreAuthorize(BoosterType.COINS));
	//		put(DonateType.GLOBAL_EXP_BOOSTER, globalBoosterPreAuthorize(BoosterType.EXP));
	//		put(DonateType.GLOBAL_VILLAGER_BOOSTER, globalBoosterPreAuthorize(BoosterType.VILLAGER));
	//		put(DonateType.LOCAL_EXP_BOOSTER, localBoosterPreAuthorize(BoosterType.EXP));
	//		put(DonateType.LOCAL_MONEY_BOOSTER, localBoosterPreAuthorize(BoosterType.COINS));
	//	}};
	//
	//	private static final Map<DonateType, BiConsumer<UserTransactionPackage, UserInfo>> TRANSACTION_POST_AUTHORIZE_MAP
	//			= new HashMap<DonateType, BiConsumer<UserTransactionPackage, UserInfo>>() {{
	//		put(DonateType.GLOBAL_MONEY_BOOSTER, boosterPostAuthorize(BoosterType.COINS, true));
	//		put(DonateType.GLOBAL_EXP_BOOSTER, boosterPostAuthorize(BoosterType.EXP, true));
	//		put(DonateType.GLOBAL_VILLAGER_BOOSTER, boosterPostAuthorize(BoosterType.VILLAGER, true));
	//		put(DonateType.LOCAL_MONEY_BOOSTER, boosterPostAuthorize(BoosterType.COINS, false));
	//		put(DonateType.LOCAL_EXP_BOOSTER, boosterPostAuthorize(BoosterType.EXP, false));
	//	}};

	private ConfigurationManager configurationManager;

	private MongoClient mongoClient;
	private final IUserService playerService = new UserService(this);
	private final IBoosterService boosterService = new BoosterService(this);
	private final IDonateService donateService = new DonateService(this);

	private MongoAdapter<BoosterInfo> globalBoosters;

	private final Map<String, MuseumMetricsPackage> metrics = Maps.newConcurrentMap();

	private static RealmsController realmsController;

	private static String environment(String name) {
		String value = System.getenv(name);
		if (value != null) return value;

		throw new NoSuchElementException("No " + name + " environment variable specified!");
	}

	public static void main(String[] args) {

		int port = Integer.parseInt(environment("MUSEUM_SERVICE_PORT"));
		String password = environment("MUSEUM_SERVICE_PASSWORD");
		String dbUrl = environment("MUSEUM_DATABASE_URL");
		String dbName = environment("MUSEUM_DATABASE_NAME");

		instance = new MuseumService(password, dbUrl, dbName);
		instance.start(port);

	}

	public <T extends Unique> MongoAdapter<T> createStorageAdapter(Class<T> type, String collection) {
		if (mongoClient == null)
			throw new IllegalStateException("MongoDB is not initialized yet.");
		return new MongoAdapter<>(mongoClient, databaseName, collection, type);
	}

	public void start(int port) {

		MicroserviceBootstrap.bootstrap(new MicroServicePlatform(2));
		CoreApi core = CoreApi.get();

		core.registerService(IPermissionService.class, new PermissionService(ISocketClient.get()));

		ServerSocket serverSocket = new ServerSocket(port);
		serverSocket.start();

		this.mongoClient = MongoClients.create(databaseUrl);

		core.registerService(IUserService.class, new UserService(this));
		core.registerService(IBoosterService.class, new BoosterService(this));

		realmsController = new RealmsController();

		configurationManager = new ConfigurationManager("config.yml", "items.yml");
		configurationManager.init();

		registerHandler(MuseumMetricsPackage.class, (realm, pckg) -> {
			metrics.put(pckg.getServerName(), pckg);
		});

		registerHandler(UserChatPackage.class, ((realm, museumPackage) -> {
			BroadcastMessagePackage messagePackage = new BroadcastMessagePackage(museumPackage.getJsonMessage());
			ServerSocketHandler.broadcast(messagePackage);
		}));
		registerHandler(UserBroadcastPackage.class, ((channel, serverName, museumPackage) -> {
			BroadcastTitlePackage broadcastPackage = new BroadcastTitlePackage(museumPackage.getData(), museumPackage.getFadeIn(), museumPackage.getStay(), museumPackage.getFadeOut());
			ServerSocketHandler.broadcast(broadcastPackage);
		}));
		registerHandler(ThanksExecutePackage.class, ((channel, serverName, museumPackage) -> {
			long boosters = boosterService.executeThanks(museumPackage.getUser());
			userData.find(museumPackage.getUser()).thenAccept(data -> {
				extra(museumPackage.getUser(), data.getIncome() * INCOME_MULTIPLIER * boosters);
				museumPackage.setBoostersCount(boosters);
				send(channel, museumPackage);
			});
		}));
		registerHandler(RequestConfigurationsPackage.class, ((channel, serverName, museumPackage) -> {
			CONFIGURATION_MANAGER.fillRequest(museumPackage);
			send(channel, museumPackage);
		}));
		registerHandler(RequestGlobalBoostersPackage.class, ((channel, serverName, museumPackage) -> {
			museumPackage.setBoosters(new ArrayList<>(boosterService.getGlobalBoosters().values()));
			send(channel, museumPackage);
		}));
		registerHandler(TopPackage.class, ((channel, serverName, museumPackage) ->
				userData.getTop(museumPackage.getTopType(), museumPackage.getLimit()).thenAccept(res -> {
					museumPackage.setEntries(res);
					send(channel, museumPackage);
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
			send(channel, museumPackage);
		}));

		try {
			Javalin.create().get("/", ctx -> ctx.result(createMetrics())).start(Integer.parseInt(System.getenv("METRICS_PORT")));
		} catch (NumberFormatException | NullPointerException ignored) {}

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
	public <T extends MuseumPackage> void registerHandler(Class<T> clazz, PackageHandler<T> handler) {
		handlerMap.put(clazz, handler);
	}

	/**
	 * Send package to socket
	 *
	 * @param pckg package
	 */
	public void send(Channel channel, MuseumPackage pckg) {
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
		ServerSocketHandler.broadcast(new BroadcastTitlePackage(new String[] {title, subTitle}, fadeIn, stay, fadeOut));
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

	private static BiPredicate<UserTransactionPackage, UserInfo> globalBoosterPreAuthorize(BoosterType type) {
		return (pckg, info) -> !boosterService.getGlobalBoosters().containsKey(type);
	}

	private static BiPredicate<UserTransactionPackage, UserInfo> localBoosterPreAuthorize(BoosterType type) {
		return (pckg, info) -> {
			try {
				return boosterService.receiveLocal(pckg.getUser()).get(2L, TimeUnit.SECONDS).stream().noneMatch(booster -> booster.getType() == type);
			} catch (Exception ex) {
				ex.printStackTrace();
				return false;
			}
		};
	}

	private static BiConsumer<UserTransactionPackage, UserInfo> boosterPostAuthorize(BoosterType type, boolean global) {
		return (pckg, info) -> wrapUuid(Collections.singletonList(pckg.getUser())).thenApply(list -> list.get(0)).thenAccept(userName -> {
			boosterService.addGlobalBooster(BoosterInfo.defaultInstance(pckg.getUser(), userName, type, DEFAULT_BOOSTER_TIME, global));
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


}
