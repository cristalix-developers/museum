package museum.handlers;

import io.netty.channel.Channel;
import museum.packages.MuseumPackage;

@FunctionalInterface
public interface PackageHandler<T extends MuseumPackage> {

	void handle(Channel channel, String serverName, T museumPackage);

}
