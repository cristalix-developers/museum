package ru.func.museum.museum.space;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.Location;
import ru.func.museum.museum.space.viewer.SpaceViewerType;

/**
 * @author func 22.05.2020
 * @project Museum
 */
@Getter
@AllArgsConstructor
public enum SpaceType {
    DINOSAUR(
            SpaceViewerType.SKELETON,
            new Location(null, 0, 0, 0),
            new Location(null, 0, 0, 0)
    ),;

    private SpaceViewerType spaceViewerType;
    private Location startTemplatePosition;
    private Location endTemplatePosition;
}
