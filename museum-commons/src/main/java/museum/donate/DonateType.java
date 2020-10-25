package museum.donate;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum DonateType {

	GLOBAL_MONEY_BOOSTER("Глобальный бустер денег", 99, false),
	GLOBAL_EXP_BOOSTER("Глобальный бустер опыта", 149, false),
	LEGENDARY_PICKAXE("Легендарная кирка", 300, false),
	STEAM_PUNK_COLLECTOR("Стим-панк сборщик монет", 300, false),
	;

	private final String name;
	private final int price;
	private final boolean save; // можно ли купить повторно(возвращается в юзердате потом)

}
