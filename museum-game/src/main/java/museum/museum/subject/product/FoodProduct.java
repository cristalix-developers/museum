package museum.museum.subject.product;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum FoodProduct {

	SHAWARMA("§aШаверма", 160),
	CHOCOLATE("§dШоколадка Milka", 120),
	CHIPS("§6Чипсы Pringles", 300),
	HOT_DOG("§cХотдог", 120),
	HOLY_SPRING("§bСвятой источник", 80),
	COCA_COLA("§4Кока-кола §f2Л", 200),
	PEPSI("§bПепси §f0.33Л", 75),
	CREAM("§fПломбир", 100),
	MAGNATE("§eМагнат", 120),
	TWIX("§6Твикс", 70),
	COTTON_CANDY("§dСладкая вата", 60),
	HORN("§eРожок", 30);

	private final String name;
	private final int cost;

}
