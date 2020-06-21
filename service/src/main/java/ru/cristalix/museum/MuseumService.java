package ru.cristalix.museum;

import io.netty.channel.Channel;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import ru.cristalix.core.GlobalSerializers;
import ru.cristalix.core.microservice.MicroServicePlatform;
import ru.cristalix.core.microservice.MicroserviceBootstrap;
import ru.cristalix.core.network.ISocketClient;
import ru.cristalix.core.network.packages.MoneyTransactionRequestPackage;
import ru.cristalix.core.network.packages.MoneyTransactionResponsePackage;
import ru.cristalix.museum.data.UserInfo;
import ru.cristalix.museum.handlers.PackageHandler;
import ru.cristalix.museum.packages.*;
import ru.cristalix.museum.socket.ServerSocket;
import ru.cristalix.museum.socket.ServerSocketHandler;
import ru.ilyafx.sql.BaseSQL;
import ru.ilyafx.sql.SQL;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class MuseumService {

	public static final String PASSWORD = System.getProperty("PASSWORD", "gVatjN43AJnbFq36Fa");
	public static final Map<Class<? extends MuseumPackage>, PackageHandler> HANDLER_MAP = new HashMap<>();
	public static SqlManager SQL_MANAGER;

	public static void main(String[] args) {
		MicroserviceBootstrap.bootstrap(new MicroServicePlatform(1));

		ServerSocket serverSocket = new ServerSocket(14653);
		serverSocket.start();

		MongoManager.connect(
				System.getenv("db_url"),
				System.getenv("db_data"),
				System.getenv("db_collection")
		);

		SQL_MANAGER = new SqlManager(new BaseSQL(SQL.builder()
				.host(System.getenv("SQL_HOST"))
				.port(Integer.parseInt(System.getenv("SQL_PORT")))
				.database(System.getenv("SQL_DATABASE"))
				.password(System.getenv("SQL_PASSWORD"))
				.user(System.getenv("SQL_USER"))
				.build()));

		registerHandler(UserInfoPackage.class, (channel, source, pckg) -> {
			System.out.println("Receive UserInfoPackage from " + source + " for " + pckg.getUuid().toString());
			MongoManager.load(pckg.getUuid())
					.thenAccept(data -> {
						UserInfo info;
						if (data == null)
							info = null;
						else
							info = GlobalSerializers.fromJson(data, UserInfo.class);
						pckg.setUserInfo(info);
						SQL_MANAGER.receiveLocal(pckg.getUuid()).thenAccept(list -> {
							pckg.setLocalBoosters(list);
							answer(channel, pckg);
						});
					});
		});
		registerHandler(SaveUserPackage.class, (channel, source, pckg) -> {
			System.out.println("Receive SaveUserPackage from " + source + " for " + pckg.getUser().toString());
			MongoManager.save(pckg.getUserInfo());
		});
		registerHandler(BulkSaveUserPackage.class, (channel, source, pckg) -> {
			System.out.println("Receive BulkSaveUserPackage from " + source);
			MongoManager.bulkSave(pckg.getPackages().stream().map(SaveUserPackage::getUserInfo).collect(Collectors.toList()));
		});
		registerHandler(UserTransactionPackage.class, (channel, source, pckg) -> {
			System.out.println("Receive UserTransactionPackage from " + source);
			// TODO: check user donates list.
			processInvoice(pckg.getUser(), pckg.getDonate().getPrice(), pckg.getDonate().getName()).thenAccept(response -> {
				UserTransactionPackage.TransactionResponse resp = UserTransactionPackage.TransactionResponse.OK;
				if (response.getErrorMessage() != null) {
					String err = response.getErrorMessage();
					if (err.equalsIgnoreCase("Недостаточно средств на счёте"))
						resp = UserTransactionPackage.TransactionResponse.INSUFFICIENT_FUNDS;
					else {
						System.out.println(err);
						resp = UserTransactionPackage.TransactionResponse.INTERNAL_ERROR;
					}
				}
				if (resp == UserTransactionPackage.TransactionResponse.OK) {
					// TODO: handle
				}
				pckg.setResponse(resp);
				answer(channel, pckg);
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

}
