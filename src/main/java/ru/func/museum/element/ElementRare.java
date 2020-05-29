package ru.func.museum.element;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ElementRare {
    USUAL(2, "Обыный"),
    RARE(3, "Редкий"),
    AMAZING(5, "Финоминальный"),
    FANTASTIC(8, "Неизведанный"),
    ;

    private int rareScale;
    private String name;
}
