package ru.func.museum.excavation.generator;

import org.bukkit.Location;
import org.bukkit.entity.Player;

/**
 * @author func 22.05.2020
 * @project Museum
 */
public interface ExcavationGenerator {

    int getDepth();

    Location getCenter();

    int[] getElementsId();

    void generateAndShow(Player player);

    boolean fastCanBreak(int x, int y, int z);
}
