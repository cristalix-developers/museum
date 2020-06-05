package ru.func.museum.museum.template;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.Location;
import ru.func.museum.excavation.Excavation;
import ru.func.museum.museum.space.SkeletonSpaceViewer;
import ru.func.museum.museum.space.SpaceReflectType;

import java.util.Arrays;

/**
 * @author func 22.05.2020
 * @project Museum
 */
@Getter
@AllArgsConstructor
public enum MuseumTemplateType {
    DEFAULT(new DefaultMuseum(
            () -> Arrays.asList(new SkeletonSpaceViewer(
                    -89, 91, 267,
                    SpaceReflectType.SOUTH,
                    0//,
                    //-87, 90, 261
            ), new SkeletonSpaceViewer(
                    -94, 91, 262,
                    SpaceReflectType.NORTH,
                    0//,
                    //-95, 90, 261
            )), "Музей динозавров",
            Arrays.asList(
                    new Location(Excavation.WORLD, -93, 90, 257),
                    new Location(Excavation.WORLD, -91, 90, 257),
                    new Location(Excavation.WORLD, -91, 90, 278),
                    new Location(Excavation.WORLD, -93, 90, 278)
            ), new Location(Excavation.WORLD, -91, 90, 251)
    )),
    ;

    private MuseumTemplate museumTemplate;
}
