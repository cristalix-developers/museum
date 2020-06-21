package ru.cristalix.museum.donate;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum DonateType {

	COLLECTOR("Донатный сборщик монет", 299, true),
	GLOBAL_MONEY_BOOSTER("Глобальный бустер на монетки", 149, false),
	LOCAL_MONEY_BOOSTER("Локальный бустер на монетки", 99, false),
	EXCAVATOR("10 экскаваторов", 59, false),
	TNT("10 динамитов", 59, false),
	HELPERS("10 помощников", 59, false),
	;

	private final String name;
	private final int price;
	private final boolean save; // можно ли купить повторно(возвращается в юзердате потом)

}
