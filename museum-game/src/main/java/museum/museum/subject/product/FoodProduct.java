package museum.museum.subject.product;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum FoodProduct {

	TEST("Тест", 1),
	;

	private final String name;
	private final int cost;

}
