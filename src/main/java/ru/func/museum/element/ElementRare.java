package ru.func.museum.element;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ElementRare {
    USUAL(1, "Обыный"),
    RARE(2, "Редкий"),
    AMAZING(4, "Финоминальный"),
    FANTASTIC(8, "Неизведанный"),
    ;

    private int rareScale;
    private String name;
}
