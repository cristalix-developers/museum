package ru.cristalix.museum;

import clepto.cristalix.Box;
import clepto.cristalix.WorldMeta;
import lombok.Getter;
import lombok.experimental.Delegate;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public abstract class Manager<T extends Prototype> {

    private final Map<String, T> map = new HashMap<>();

    public Manager(String mapSignKey, BoxReader<T> reader) {
        App.getApp().getMap().getBoxes(mapSignKey).entrySet().stream()
                .map(entry -> reader.readBox(entry.getKey(), entry.getValue()))
                .filter(Objects::nonNull)
                .forEach(this::registerPrototype);
    }

    public T getPrototype(String address) {
        return map.get(address);
    }

    public void registerPrototype(T prototype) {
        map.put(prototype.getAddress(), prototype);
    }
}
