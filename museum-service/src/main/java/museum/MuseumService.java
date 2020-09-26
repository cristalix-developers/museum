package museum;

import com.mongodb.async.client.MongoClient;
import com.mongodb.async.client.MongoClients;
import io.netty.channel.Channel;
import lombok.val;
import museum.boosters.BoosterType;
import museum.configuration.ConfigurationManager;
import museum.data.BoosterInfo;
import museum.data.UserInfo;
import museum.donate.DonateType;
import museum.handlers.PackageHandler;
import museum.packages.*;
import museum.socket.ServerSocket;
import museum.socket.ServerSocketHandler;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import ru.cristalix.core.GlobalSerializers;
import ru.cristalix.core.microservice.MicroServicePlatform;
import ru.cristalix.core.microservice.MicroserviceBootstrap;
import ru.cristalix.core.network.ISocketClient;
import ru.cristalix.core.network.packages.FillLauncherUserDataPackage;
import ru.cristalix.core.network.packages.MoneyTransactionRequestPackage;
import ru.cristalix.core.network.packages.MoneyTransactionResponsePackage;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;

public class MuseumService {

	public static final long DEFAULT_BOOSTER_TIME = TimeUnit.HOURS.toMillis(1L);
	public static final int THANKS_SECONDS = 45;
	public static final String PASSWORD = System.getProperty("PASSWORD", "gVatjN43AJnbFq36Fa");
	public static final Map<Class<? extends MuseumPackage>, PackageHandler> HANDLER_MAP = new HashMap<>();
	private static final Map<DonateType, BiPredicate<UserTransactionPackage, UserInfo>> TRANSACTION_PRE_AUTHORIZE_MAP = new HashMap<DonateType, BiPredicate<UserTransactionPackage, UserInfo>>() {{
		put(DonateType.LOCAL_MONEY_BOOSTER, localBoosterPreAuthorize(BoosterType.COINS));
		put(DonateType.GLOBAL_MONEY_BOOSTER, globalBoosterPreAuthorize(BoosterType.COINS));
		put(DonateType.LOCAL_VISITORS_BOOSTER, localBoosterPreAuthorize(BoosterType.VISITORS));
		put(DonateType.GLOBAL_VISITORS_BOOSTER, globalBoosterPreAuthorize(BoosterType.VISITORS));
	}};
	private static final Map<DonateType, BiConsumer<UserTransactionPackage, UserInfo>> TRANSACTION_POST_AUTHORIZE_MAP
			= new HashMap<DonateType, BiConsumer<UserTransactionPackage, UserInfo>>() {{
		put(DonateType.LOCAL_MONEY_BOOSTER, boosterPostAuthorize(BoosterType.COINS, false));
		put(DonateType.GLOBAL_MONEY_BOOSTER, boosterPostAuthorize(BoosterType.COINS, true));
		put(DonateType.LOCAL_VISITORS_BOOSTER, boosterPostAuthorize(BoosterType.VISITORS, false));
		put(DonateType.GLOBAL_VISITORS_BOOSTER, boosterPostAuthorize(BoosterType.VISITORS, true));
	}};
	public static ConfigurationManager CONFIGURATION_MANAGER;

	public static UserDataMongoAdapter userData;
	public static MongoAdapter<BoosterInfo> globalBoosters;

	public static List<Subservice> subservices = new ArrayList<>();

	private static BoosterManager boosterManager;


	public static void main(String[] args) {
		MicroserviceBootstrap.bootstrap(new MicroServicePlatform(2));

		ServerSocket serverSocket = new ServerSocket(14653);
		serverSocket.start();

		String dbUrl = System.getenv("db_url");
		String dbName = System.getenv("db_data");
		MongoClient client = MongoClients.create(dbUrl);
		userData = new UserDataMongoAdapter(client, dbName);
		globalBoosters = new MongoAdapter<>(client, dbName, "globalBoosters", BoosterInfo.class);

		boosterManager = new BoosterManager();
		subservices.add(boosterManager);


		CONFIGURATION_MANAGER = new ConfigurationManager("config.yml", "items.yml");
		CONFIGURATION_MANAGER.init();

		registerHandler(UserInfoPackage.class, (channel, source, pckg) -> {
			System.out.println("Received UserInfoPackage from " + source + " for " + pckg.getUuid().toString());

			userData.find(pckg.getUuid()).thenAccept(info -> {
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
				val preHandler = TRANSACTION_PRE_AUTHORIZE_MAP.get(pckg.getDonate());
				if (preHandler != null && !preHandler.test(pckg, info)) {
					pckg.setResponse(UserTransactionPackage.TransactionResponse.INTERNAL_ERROR);
					return;
				}
				if (pckg.getDonate().isSave() && info.getDonates().contains(pckg.getDonate())) {
					pckg.setResponse(UserTransactionPackage.TransactionResponse.ALREADY_BUYED);
					return;
				}
				processInvoice(pckg.getUser(), pckg.getDonate().getPrice(), pckg.getDonate().getName()).thenAccept(response -> {
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
			extra(museumPackage.getUser(), null, (double) (THANKS_SECONDS * boosters));
			museumPackage.setBoostersCount(boosters);
			answer(channel, museumPackage);
		}));
		registerHandler(RequestConfigurationsPackage.class, ((channel, serverName, museumPackage) -> {
			CONFIGURATION_MANAGER.fillRequest(museumPackage);
			answer(channel, museumPackage);
		}));
		registerHandler(TopPackage.class, ((channel, serverName, museumPackage) ->
				userData.getTop(museumPackage.getTopType(), museumPackage.getLimit()).thenAccept(res -> {
			museumPackage.setEntries(res);
			answer(channel, museumPackage);
		})));

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
					UUID uuid = UUID.fromString(args[1]);
					userData.clear(uuid);
					System.out.println("Removed " + uuid + "'s data from db...");
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

	public static void extra(UUID user, Double sum, Double seconds) {
		// TODO: we need to calculate money for offline users.
		ServerSocketHandler.broadcast(new ExtraDepositUserPackage(user, sum, seconds));
	}

	public static void sendMessage(Set<UUID> users, String message) {
		sendMessage(users, TextComponent.fromLegacyText(message));
	}

	public static void sendMessage(Set<UUID> users, BaseComponent[] message) {
		ServerSocketHandler.broadcast(new TargetMessagePackage(users, ComponentSerializer.toString(message)));
	}

	private static BiPredicate<UserTransactionPackage, UserInfo> globalBoosterPreAuthorize(BoosterType type) {
		return (pckg, info) -> !boosterManager.getGlobalBoosters().containsKey(type);
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

}
