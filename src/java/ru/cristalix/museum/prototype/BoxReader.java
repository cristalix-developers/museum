package ru.cristalix.museum.prototype;

import clepto.cristalix.Box;
import ru.cristalix.museum.prototype.Prototype;

public interface BoxReader<T extends Prototype> {

    T readBox(String address, Box box);
}
