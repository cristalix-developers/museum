package ru.func.museum.museum.template;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.Location;

import java.util.ArrayList;

/**
 * @author func 22.05.2020
 * @project Museum
 */
@Getter
@AllArgsConstructor
public enum MuseumTemplateType {

    DEFAULT(new DefaultMuseum(
            ArrayList::new,
            "Музей динозавров",
            new Location[]{}
    )),
    ;

    private MuseumTemplate museumTemplate;
}
