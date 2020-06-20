package ru.cristalix.museum;

import io.netty.channel.Channel;
import ru.cristalix.core.GlobalSerializers;
import ru.cristalix.museum.data.PickaxeType;
import ru.cristalix.museum.data.UserInfo;
import ru.cristalix.museum.handlers.PackageHandler;
import ru.cristalix.museum.packages.BulkSaveUserPackage;
import ru.cristalix.museum.packages.MuseumPackage;
import ru.cristalix.museum.packages.SaveUserPackage;
import ru.cristalix.museum.packages.UserInfoPackage;
import ru.cristalix.museum.socket.ServerSocket;
import ru.cristalix.museum.socket.ServerSocketHandler;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class MuseumService {

    public static final String PASSWORD = System.getProperty("PASSWORD", "gVatjN43AJnbFq36Fa");
    public static final Map<Class<? extends MuseumPackage>, PackageHandler> HANDLER_MAP = new HashMap<>();

    public static void main(String[] args) {
        ServerSocket serverSocket = new ServerSocket(14653);
        serverSocket.start();

        MongoManager.connect(
                System.getenv("db_url"),
                System.getenv("db_data"),
                System.getenv("db_collection")
        );

        registerHandler(UserInfoPackage.class, (channel, source, pckg) -> {
            System.out.println("Receive UserInfoPackage from " + source + " for " + pckg.getUuid().toString());
            MongoManager.load(pckg.getUuid())
                    .thenAccept(data -> {
                        UserInfo info;
                        if (data == null)
                            // Default user values
                            info = new UserInfo(pckg.getUuid(), 0, 0.0, PickaxeType.DEFAULT, Collections.emptyList(), Collections.emptyList(), 0, 0);
                        else
                            info = GlobalSerializers.fromJson(data, UserInfo.class);
                        pckg.setUserInfo(info);
                        answer(channel, pckg);
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

}
