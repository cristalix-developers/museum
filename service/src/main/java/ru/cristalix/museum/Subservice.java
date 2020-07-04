package ru.cristalix.museum;

import ru.cristalix.museum.packages.MuseumPackage;
import ru.cristalix.museum.socket.ServerSocketHandler;

@FunctionalInterface
public interface Subservice {

	MuseumPackage createPackage();

	default void updateOnRealms() {
		ServerSocketHandler.broadcast(createPackage());
	}

}
