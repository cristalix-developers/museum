package ru.func.museum.element;

import lombok.AllArgsConstructor;
import lombok.Getter;
import ru.func.museum.museum.space.SpaceType;

@Getter
@AllArgsConstructor
public enum ElementType {
    BONE(new Element(
            null,
            SpaceType.DINOSAUR,
            100,
            "Нога динозавра"
    ), (location, viewer) -> {
    }),
    ;

    private Element element;
    private ElementViewer elementViewer;
}
