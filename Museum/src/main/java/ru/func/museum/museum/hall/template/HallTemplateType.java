package ru.func.museum.museum.hall.template;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.Location;
import ru.func.museum.excavation.Excavation;
import ru.func.museum.museum.space.SkeletonSpaceViewer;
import ru.func.museum.museum.space.SpaceReflectType;

import java.util.ArrayList;
import java.util.Arrays;

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
                    new ArrayList<>()
            ), new SkeletonSpaceViewer(
                    -94, 91, 262,
                    SpaceReflectType.NORTH,
                    0,
                    -95, 90, 261,
                    new ArrayList<>()
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
            ), new Location(Excavation.WORLD, -91, 90, 251)
    )),
    ;

    private HallTemplate hallTemplate;
}
