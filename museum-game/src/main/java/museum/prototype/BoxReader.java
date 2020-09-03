package museum.prototype;

import clepto.cristalix.mapservice.Box;

@FunctionalInterface
public interface BoxReader<T extends Prototype> {

	T readBox(String address, Box box);
}
