package ru.func.museum.museum.template;

import org.bukkit.Location;
import ru.func.museum.museum.space.Space;

import java.util.List;
import java.util.function.Supplier;

/**
 * @author func 22.05.2020
 * @project Museum
 */
public interface MuseumTemplate {
    Supplier<List<Space>> getMatrix();

    String getTitle();

    List<Location> getCollectorRoute();
}
