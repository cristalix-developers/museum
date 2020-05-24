package ru.func.museum.museum.template;

import org.bukkit.Location;
import ru.func.museum.museum.space.Space;

import java.util.List;

/**
 * @author func 22.05.2020
 * @project Museum
 */
public interface MuseumTemplate {
    List<Space> getMatrix();

    String getTitle();

    Location[] getCollectorRoute();
}
