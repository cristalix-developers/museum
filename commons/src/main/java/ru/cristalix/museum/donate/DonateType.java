package ru.cristalix.museum.donate;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum DonateType {

	COLLECTOR("Донатный сборщик монет", 299, true),
	;

	private final String name;
	private final int price;
	private final boolean save; // можно ли купить повторно(возвращается в юзердате потом)

}
