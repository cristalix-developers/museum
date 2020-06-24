package ru.cristalix.museum.prototype;

import clepto.cristalix.Box;

@FunctionalInterface
public interface BoxReader<T extends Prototype> {

	T readBox(String address, Box box);
}
