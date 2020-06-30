package ru.cristalix.museum.boosters;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum BoosterType {

	COINS("Бустер на деньги", "Посетители музея стали восторгаться настолько сильно, что из них посыпалось в два раза больше монет!", 2.0, 2.0),
	VISITORS("Бустер на посетителей", "Вы распиарили свой музей в газетах, на телевизоре и на всех билбордах города! Посетителей просто тьма!", 2.0, 2.0),
	;

	private final String name;
	private final String description;
	private final double localMultiplier, globalMultiplier;

}
