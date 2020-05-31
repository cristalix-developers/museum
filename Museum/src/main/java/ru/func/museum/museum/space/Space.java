package ru.func.museum.museum.space;

import org.bukkit.Location;
import ru.func.museum.museum.AbstractMuseum;

public interface Space {

    AbstractMuseum getMuseum();

    SpaceType getSpaceType();

    void setSpaceType(SpaceType spaceType);

    Location getStartPosition();

    Location getEndpoint();
}
