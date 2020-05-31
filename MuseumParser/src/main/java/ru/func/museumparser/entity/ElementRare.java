package ru.func.museumparser.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ElementRare {
    USUAL(2, "Обыная", "новый"),
    RARE(3, "Редкая", "редкий"),
    AMAZING(5, "Финоминальная", "финоминальный"),
    FANTASTIC(8, "Неизведанная", "ранее неизвестный"),
    ;

    private int rareScale;
    private String name;
    private String word;
}
