package ru.cristalix.museum.handlers;

import io.netty.channel.Channel;
import ru.cristalix.museum.packages.MuseumPackage;

public interface PackageHandler<T extends MuseumPackage> {

    void handle(Channel channel, String serverName, T museumPackage);

}
