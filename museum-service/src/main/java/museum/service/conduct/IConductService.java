package museum.service.conduct;

import museum.packages.MuseumPackage;
import ru.cristalix.core.IService;
import ru.cristalix.core.realm.RealmId;

import java.util.Collection;
import java.util.Optional;

public interface IConductService extends IService {

	<T extends MuseumPackage> void registerHandler(Class<T> type, PacketHandler<T> handler);

	<T extends MuseumPackage> void handlePacket(Realm realm, T packet);

	Optional<Realm> getBestRealm();

	Collection<? extends Realm> getRealms();

	Realm getRealm(RealmId realm);

	void registerRealm(Realm realm);

	void unregisterRealm(Realm realm);

	void broadcast(MuseumPackage packet);

}
