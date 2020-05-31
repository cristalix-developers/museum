package ru.func.museum.museum.collector;

import lombok.Getter;
import org.bukkit.Location;
import ru.func.museum.museum.Museum;

/**
 * @author func 22.05.2020
 * @project Museum
 */
@Getter
public class DefaultCollector implements AbstractCollector {

    private Museum museum;
    private int speed;
    private double cost;

    @Override
    public void move(Location point) {

    }
}
