package museum.museum.subject.product;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum FoodProduct {

	SHAWARMA("Шаверма", 160),
	CHOCOLATE("Шоколадка Оленка", 120),
	CHIPS("Чипсы Pringles", 300),
	HOT_DOG("Хотдог", 120),
	HOLY_SPRING("Святой источник", 80),
	COCA_COLA("Кока-кола 2Л", 200),
	PEPSI("Пепси 300мл", 75),
	CREAM("Пломбир", 100),
	MAGNATE("Магнат", 120),
	TWIX("Твикс", 70),
	COTTON_CANDY("Сладкая вата", 60),
	HORN("Рожок", 30);

	private final String name;
	private final int cost;

}
