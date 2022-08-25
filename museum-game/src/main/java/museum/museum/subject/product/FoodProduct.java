package museum.museum.subject.product;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum FoodProduct {

	SHAWARMA("§aШаверма", 190),
	CHOCOLATE("§dШоколадка Milka", 160),
	CHIPS("§6Чипсы Pringles", 370),
	HOT_DOG("§cХотдог", 180),
	HOLY_SPRING("§bСвятой источник", 100),
	COCA_COLA("§4Кока-кола §f2Л", 230),
	PEPSI("§bПепси §f0.33Л", 105),
	CREAM("§fПломбир", 135),
	MAGNATE("§eМагнат", 160),
	TWIX("§6Твикс", 100),
	COTTON_CANDY("§dСладкая вата", 90),
	HORN("§eРожок", 60);

	private final String name;
	private final int cost;

}
