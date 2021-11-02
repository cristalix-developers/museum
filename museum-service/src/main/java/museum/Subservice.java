package museum;

import museum.packages.MuseumPackage;
import museum.socket.ServerSocketHandler;

@FunctionalInterface
public interface Subservice {

	MuseumPackage createPackage();

	default void updateOnRealms() {
		ServerSocketHandler.broadcast(createPackage());
	}
}
