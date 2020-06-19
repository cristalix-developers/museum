package ru.cristalix.museum;

import ru.cristalix.core.GlobalSerializers;
import ru.cristalix.core.microservice.MicroServicePlatform;
import ru.cristalix.core.microservice.MicroserviceBootstrap;
import ru.cristalix.core.network.Capability;
import ru.cristalix.core.network.CorePackage;
import ru.cristalix.core.network.ISocketClient;
import ru.cristalix.core.realm.RealmId;
import ru.cristalix.museum.data.PickaxeType;
import ru.cristalix.museum.data.UserInfo;
import ru.cristalix.museum.packages.BulkSaveUserPackage;
import ru.cristalix.museum.packages.SaveUserPackage;
import ru.cristalix.museum.packages.UserInfoPackage;

import java.util.Collections;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class MuseumService {

    public static void main(String[] args) {
        MicroserviceBootstrap.bootstrap(new MicroServicePlatform(1));

        MongoManager.connect(
                System.getProperty("db_url"),
                System.getProperty("db_data"),
                System.getProperty("db_collection")
        );

        registerCapability(UserInfoPackage.class, false);
        registerHandler(UserInfoPackage.class, (source, pckg) -> {
            MongoManager.load(pckg.getUuid())
                    .thenAccept(data -> {
                        UserInfo info;
                        if (data == null)
                            // Default user values
                            info = new UserInfo(pckg.getUuid(), 0, 0.0, PickaxeType.DEFAULT, Collections.emptyList(), Collections.emptyList(), 0, 0);
                        else
                            info = GlobalSerializers.fromJson(data, UserInfo.class);
                        pckg.setUserInfo(info);
                        answer(pckg);
                    });
        });
        registerCapability(SaveUserPackage.class, true);
        registerHandler(SaveUserPackage.class, pckg -> MongoManager.save(pckg.getUserInfo()));
        registerCapability(BulkSaveUserPackage.class, true);
        registerHandler(BulkSaveUserPackage.class, pckg -> MongoManager.bulkSave(pckg.getPackages().stream().map(SaveUserPackage::getUserInfo).collect(Collectors.toList())));
    }

    /**
     * Register class to this service (means that this service can handle packages of this type)
     *
     * @param clazz        class of package
     * @param notification if true service doesn't answer (for example: we don't need to answer on user update package)
     */
    private static void registerCapability(Class<? extends CorePackage> clazz, boolean notification) {
        ISocketClient.get().registerCapability(Capability.builder().className(clazz.getName()).notification(notification).build());
    }

    /**
     * Register handler to package type
     *
     * @param clazz    class of package
     * @param consumer handler
     * @param <T>      package type
     */
    private static <T extends CorePackage> void registerHandler(Class<T> clazz, BiConsumer<RealmId, T> consumer) {
        ISocketClient.get().addListener(clazz, consumer);
    }

    /**
     * Register handler to package without source (RealmId)
     *
     * @param clazz    class of package
     * @param consumer handler
     * @param <T>      package type
     */
    private static <T extends CorePackage> void registerHandler(Class<T> clazz, Consumer<T> consumer) {
        ISocketClient.get().addListener(clazz, (source, pckg) -> consumer.accept(pckg));
    }

    /**
     * Send package to socket
     *
     * @param pckg package
     */
    private static void answer(CorePackage pckg) {
        ISocketClient.get().write(pckg);
    }

}
