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
    DEFAULT(new DefaultPickaxe()),;

    private Pickaxe pickaxe;
}
