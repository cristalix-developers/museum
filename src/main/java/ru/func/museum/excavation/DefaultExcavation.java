package ru.func.museum.excavation;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.Location;

/**
 * @author func 22.05.2020
 * @project Museum
 */
@Getter
@AllArgsConstructor
public class DefaultExcavation implements Excavation {

    private String title;
    private double cost;
    private int minimalLevel;
    private Location startLocation;
    private Location mineCenter;
    private double depth;
    private int breakCount;

    @Override
    public boolean canBreak(Location location) {
        return mineCenter.distance(location) <= depth;
    }
}
