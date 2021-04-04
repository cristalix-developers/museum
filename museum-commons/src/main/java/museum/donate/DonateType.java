package museum.donate;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum DonateType {

	PREFIX_CASE("Случайный префикс", 49, false),
	PRIVILEGES("Комиссия 0%", 399, true),
	GLOBAL_MONEY_BOOSTER("Глобальный бустер денег", 199, false),
	GLOBAL_VILLAGER_BOOSTER("Глобальный бустер посетителей", 149, false),
	GLOBAL_EXP_BOOSTER("Глобальный бустер опыта", 149, false),
	LOCAL_MONEY_BOOSTER("Локальный бустер денег", 99, false),
	LOCAL_EXP_BOOSTER("Локальный бустер опыта", 99, false),
	LEGENDARY_PICKAXE("Легендарная кирка", 349, true),
	STEAM_PUNK_COLLECTOR("Стим-панк сборщик монет", 179, true),
	;

	private final String name;
	private final int price;
	private final boolean save; // можно ли купить повторно(возвращается в юзердате потом)

}
