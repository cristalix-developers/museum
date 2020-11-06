package museum.handlers;

import museum.packages.MuseumPackage;
import museum.realm.Realm;

@FunctionalInterface
public interface PackageHandler<T extends MuseumPackage> {

	void handle(Realm realm, T museumPackage);

}
