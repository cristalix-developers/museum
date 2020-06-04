package ru.func.museum.player.pickaxe;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author func 22.05.2020
 * @project Museum
 */
@Getter
@AllArgsConstructor
public enum PickaxeType {
    DEFAULT("любительская", new DefaultPickaxe()),
    RARE("профессиональная", new RarePickaxe()),
    ULTRA("престижная", new UltraPickaxe())
    ;

    private String name;
    private Pickaxe pickaxe;
}
