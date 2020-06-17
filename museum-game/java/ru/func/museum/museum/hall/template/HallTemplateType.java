package ru.func.museum.museum.hall.template;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.Location;
import ru.func.museum.excavation.Excavation;
import ru.func.museum.museum.hall.template.space.SkeletonSpaceViewer;
import ru.func.museum.museum.hall.template.space.SpaceReflectType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

/**
 * @author func 22.05.2020
 * @project Museum
 */
@Getter
@AllArgsConstructor
public enum HallTemplateType {
    DEFAULT(new DefaultHall(
            () -> Arrays.asList(new SkeletonSpaceViewer(
                    -89, 91, 267,
                    SpaceReflectType.SOUTH,
                    0,
                    -87, 90, 261,
                    new ArrayList<>(),
                    Collections.singletonList(0)
            ), new SkeletonSpaceViewer(
                    -94, 91, 262,
                    SpaceReflectType.NORTH,
                    0,
                    -95, 90, 261,
                    new ArrayList<>(),
                    Collections.singletonList(0)
            )), "Музей динозавров",
            Arrays.asList(
                    new Location(Excavation.WORLD, -92.5, 90, 267.5),
                    new Location(Excavation.WORLD, -92.5, 94, 262.5),
                    new Location(Excavation.WORLD, -92.5, 94, 257.5),
                    new Location(Excavation.WORLD, -90.5, 94, 257.5),
                    new Location(Excavation.WORLD, -90.5, 94, 262.5),
                    new Location(Excavation.WORLD, -90.5, 90, 267.5),
                    new Location(Excavation.WORLD, -90.5, 90, 278.5),
                    new Location(Excavation.WORLD, -92.5, 90, 278.5)
            ), new Location(Excavation.WORLD, -101, 90, 245), // minimum
            new Location(Excavation.WORLD, -85, 101, 286) // maximum
    )),
    ;

    private HallTemplate hallTemplate;
}
