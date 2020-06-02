package ru.func.museum.museum.template;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.Location;
import ru.func.museum.museum.space.Space;

import java.util.List;
import java.util.function.Supplier;

/**
 * @author func 22.05.2020
 * @project Museum
 */
@Getter
@AllArgsConstructor
public class DefaultMuseum implements MuseumTemplate {
    private Supplier<List<Space>> matrix;
    private String title;
    private List<Location> collectorRoute;
}
