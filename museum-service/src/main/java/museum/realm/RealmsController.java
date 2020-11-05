package museum.realm;

import ru.cristalix.core.CoreApi;
import ru.cristalix.core.network.ISocketClient;
import ru.cristalix.core.network.packages.AllRealmsPackage;
import ru.cristalix.core.realm.RealmInfo;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class RealmsController {

    private List<RealmInfo> realms = new ArrayList<>();

    public RealmsController() {
        CoreApi.get().getPlatform().getScheduler().runAsyncRepeating(() -> {
            try {
                realms = fetchMuseumRealms().get(5L, TimeUnit.SECONDS);
            } catch(Exception ex) {
                ex.printStackTrace();
                System.out.println("AllRealmsPackage timeout");
            }
        }, 5L, TimeUnit.SECONDS);
    }

    public Optional<RealmInfo> bestRealm() {
        return realms
                .stream()
                .filter(info -> info.getMaxPlayers() - info.getCurrentPlayers() > 0)
                .min(Comparator.comparingInt(RealmInfo::getCurrentPlayers));
    }

    private CompletableFuture<List<RealmInfo>> fetchMuseumRealms() {
        return ISocketClient.get().<AllRealmsPackage>writeAndAwaitResponse(new AllRealmsPackage())
				.thenApply(pckg -> Stream.of(pckg.getRealms()).filter(info -> info.getRealmId().getTypeName().equals("MUSM")).collect(Collectors.toList()));
    }

}
