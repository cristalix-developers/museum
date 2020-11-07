package museum.service.conduct;

import museum.packages.MuseumPackage;

@FunctionalInterface
public interface PacketHandler<T extends MuseumPackage> {

	void handle(Realm realm, T packet);

}
