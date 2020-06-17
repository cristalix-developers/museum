package ru.func.museum.element.deserialized;

import lombok.AllArgsConstructor;
import lombok.Getter;
import ru.func.museum.element.ElementRare;

/**
 * @author func 31.05.2020
 * @project Museum
 */
@Getter
@AllArgsConstructor
public class MuseumEntity {
    private String title;
    private SubEntity[] subs;
    private ElementRare rare;
}
