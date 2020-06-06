package ru.func.museum.element;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ElementRare {
    USUAL(1, 100, .1, "Обыная", "новый"),
    RARE(3, 500, .3, "Редкая", "редкий"),
    AMAZING(5, 2000, .6, "Финоминальная", "финоминальный"),
    FANTASTIC(8, 100000, 1.1,"Неизведанная", "ранее неизвестный"),
    ;

    private int rareScale;
    private int cost;
    private double increase;
    private String name;
    private String word;
}
