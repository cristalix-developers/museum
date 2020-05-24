package ru.func.museum.excavation;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

public interface Excavation {

    World WORLD = Bukkit.getWorld("world");

    String getTitle();
    double getCost();
    int getMinimalLevel();
    Location getStartLocation();
    Location getMineCenter();
    double getDepth();
    int getBreakCount();
    boolean canBreak(Location location);
}
