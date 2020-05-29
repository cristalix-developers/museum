package ru.func.museum.excavation;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import ru.func.museum.excavation.generator.ExcavationGenerator;

public interface Excavation {

    World WORLD = Bukkit.getWorld("world");

    ExcavationGenerator getExcavationGenerator();
    String getTitle();
    double getCost();
    int getMinimalLevel();
    Location getStartLocation();
    int getBreakCount();
}
