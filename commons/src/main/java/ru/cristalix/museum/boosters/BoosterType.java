package ru.cristalix.museum.boosters;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum BoosterType {

    ;

    private final String name;
    private final String description;
    private final int localPrice, globalPrice;
    private final double localMultiplier, globalMultiplier;

}
