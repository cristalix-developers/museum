package ru.cristalix.museum.donate;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum DonateType {

    COLLECTOR("Донатный сборщик монет", 299, true),
    ;

    private String name;
    private int price;
    private boolean save; // можно ли купить повторно(возвращается в юзердате потом)

}
