package ru.func.museum.excavation;

import org.bukkit.Location;

public interface Excavation {

    String getTitle();
    double getCost();
    int getMinimalLevel();
    Location getStartLocation();
    Location getMineCenter();
    double getDepth();
    int getBreakCount();
}
