package ru.func.museum.museum.collector;

import org.bukkit.Location;
import ru.func.museum.museum.Museum;

public interface AbstractCollector {

    Museum getMuseum();

    int getSpeed();

    double getCost();

    void move(Location point);
}
