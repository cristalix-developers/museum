package ru.func.museum.museum.space;

import ru.func.museum.museum.AbstractMuseum;
import ru.func.museum.museum.space.viewer.SpaceViewer;

public interface Space {

    AbstractMuseum getMuseum();

    SpaceViewer getSpaceViewer();

    void setSpaceViewer(SpaceViewer spaceViewer);
}
