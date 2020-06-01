package ru.func.museum.element;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ElementRare {
    USUAL(1, 100, "Обыная", "новый"),
    RARE(3, 500, "Редкая", "редкий"),
    AMAZING(5, 2000, "Финоминальная", "финоминальный"),
    FANTASTIC(8, 100000, "Неизведанная", "ранее неизвестный"),
    ;

    private int rareScale;
    private int cost;
    private String name;
    private String word;
}
