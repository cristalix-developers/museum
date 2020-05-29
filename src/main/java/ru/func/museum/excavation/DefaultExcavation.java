package ru.func.museum.excavation;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.Location;
import ru.func.museum.excavation.generator.ExcavationGenerator;

/**
 * @author func 22.05.2020
 * @project Museum
 */
@Getter
@AllArgsConstructor
public class DefaultExcavation implements Excavation {
    private ExcavationGenerator excavationGenerator;
    private String title;
    private double cost;
    private int minimalLevel;
    private Location startLocation;
    private int breakCount;
}
