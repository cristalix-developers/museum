package ru.func.museum.museum.space;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import ru.func.museum.museum.AbstractMuseum;

/**
 * @author func 23.05.2020
 * @project Museum
 */
@Getter
@AllArgsConstructor
public class SpaceImpl implements Space {
    private AbstractMuseum museum;
    @Setter
    private SpaceType spaceType;
    private Location startPosition;
    private Location endpoint;
}
