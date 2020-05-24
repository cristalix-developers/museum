package ru.func.museum.museum.template;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.Location;
import ru.func.museum.museum.space.Space;
import ru.func.museum.museum.space.SpaceImpl;
import ru.func.museum.museum.space.SpaceType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

/**
 * @author func 22.05.2020
 * @project Museum
 */
@Getter
@AllArgsConstructor
public enum MuseumTemplateType {

    DEFAULT(new DefaultMuseum(
            new ArrayList<>(),
            "Музей динозавров",
            new Location[]{}
    )),
    ;

    private MuseumTemplate museumTemplate;
}
