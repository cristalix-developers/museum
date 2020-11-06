package museum;

import museum.packages.MuseumPackage;

@FunctionalInterface
public interface Subservice {

	MuseumPackage createPackage();

	default void updateOnRealms() {
		;
	}

}
