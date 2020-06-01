package ru.func.museum.element;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.func.museum.element.deserialized.SubEntity;
import ru.func.museum.museum.space.Space;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Element {
    private SubEntity piece;
    // То, где расположен элемент
    private Space space;
}
