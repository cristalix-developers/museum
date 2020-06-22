package ru.cristalix.museum;

import clepto.cristalix.Box;

public interface BoxReader<T extends Prototype> {

    T readBox(String address, Box box);
}
