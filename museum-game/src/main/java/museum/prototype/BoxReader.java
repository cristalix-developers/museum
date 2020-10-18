package museum.prototype;

import clepto.bukkit.world.Box;

@FunctionalInterface
public interface BoxReader<T extends Prototype> {

	T readBox(String address, Box box);
}
