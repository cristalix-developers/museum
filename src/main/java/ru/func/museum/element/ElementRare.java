package ru.func.museum.element;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ElementRare {
    USUAL(2, "Обыная"),
    RARE(3, "Редкая"),
    AMAZING(5, "Финоминальная"),
    FANTASTIC(8, "Неизведанная"),
    ;

    private int rareScale;
    private String name;
}
