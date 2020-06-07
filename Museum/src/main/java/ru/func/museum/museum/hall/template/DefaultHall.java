package ru.func.museum.museum.hall.template;

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
public class DefaultHall implements HallTemplate {
    private Supplier<List<Space>> matrix;
    private String title;
    private List<Location> collectorRoute;
    private Location spawn;
}
