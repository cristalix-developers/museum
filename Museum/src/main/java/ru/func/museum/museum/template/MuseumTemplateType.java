package ru.func.museum.museum.template;

import lombok.AllArgsConstructor;
import lombok.Getter;
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
public enum MuseumTemplateType {

    DEFAULT(new DefaultMuseum(
            () -> Arrays.asList(new SkeletonSpaceViewer(
                    -88, 91, 262,
                    SpaceReflectType.EAST,
                    0
            ), new SkeletonSpaceViewer(
                    -94, 91, 266,
                    SpaceReflectType.WEST,
                    0
            )), "Музей динозавров",
            new ArrayList<>()
    )),
    ;

    private MuseumTemplate museumTemplate;
}
