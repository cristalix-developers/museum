package ru.cristalix.museum;

import io.netty.channel.Channel;
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

    public static CompletableFuture<MoneyTransactionResponsePackage> processInvoice(UUID user, int price, String description) {
        if (System.getenv("TRANSACTION_TEST") != null)
            return CompletableFuture.completedFuture(new MoneyTransactionResponsePackage(null, null));
        return ISocketClient.get().writeAndAwaitResponse(new MoneyTransactionRequestPackage(user, price, true, description));
    }

}
