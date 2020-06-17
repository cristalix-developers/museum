package ru.func.museum.museum.hall.template;

import org.bukkit.Location;
import ru.func.museum.museum.hall.template.space.Space;

import java.util.List;
import java.util.function.Supplier;

/**
 * @author func 22.05.2020
 * @project Museum
 */
public interface HallTemplate {
    Supplier<List<Space>> getMatrix();

    String getTitle();

    List<Location> getCollectorRoute();

    Location getStartDot();

    Location getEndDot();

    default boolean isInside(Location location) {
        return getStartDot().getBlockX() < location.getBlockX() && getStartDot().getBlockZ() < location.getBlockZ()
                && getEndDot().getBlockX() > location.getBlockX() && getEndDot().getBlockZ() > getEndDot().getBlockZ();
    }
}
