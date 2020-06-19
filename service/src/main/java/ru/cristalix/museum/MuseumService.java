package ru.cristalix.museum;

import ru.cristalix.core.microservice.MicroServicePlatform;
import ru.cristalix.core.microservice.MicroserviceBootstrap;
import ru.cristalix.core.network.Capability;
import ru.cristalix.core.network.CorePackage;
import ru.cristalix.core.network.ISocketClient;
import ru.cristalix.core.realm.RealmId;
import ru.cristalix.museum.packages.UserInfoPackage;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class MuseumService {

    public static void main(String[] args) {
        MicroserviceBootstrap.bootstrap(new MicroServicePlatform(1));

        registerCapability(UserInfoPackage.class, false);
        registerHandler(UserInfoPackage.class, (source, pckg) -> {
            MongoManager.load(pckg.getUuid())
                    .thenAccept(data -> pckg.setUserInfo(GlobalSerializers.fromJson(data, UserInfo.class)));
            answer(pckg);
        });
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
