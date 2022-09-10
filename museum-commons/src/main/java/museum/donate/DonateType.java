package museum.donate;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum DonateType {

	PREFIX_CASE("Случайный префикс", 49, false),
	ITEM_CASE("Лутбокс", 39, false),
	METEORITES("Лутбокс с метеоритами", 39, false),
	BONES("Лутбокс с костями", 9, false),
	GEM("Лутбокс с камнями", 39, false),
	MULTI_BOX("Мультибокс", 49, false),
	PRIVILEGES("Комиссия 0%", 119, true),
	GLOBAL_MONEY_BOOSTER("Глобальный бустер денег", 199, false),
	GLOBAL_VILLAGER_BOOSTER("Глобальный бустер посетителей", 149, false),
	GLOBAL_EXP_BOOSTER("Глобальный бустер опыта", 149, false),
	LOCAL_MONEY_BOOSTER("Локальный бустер денег", 99, false),
	LOCAL_EXP_BOOSTER("Локальный бустер опыта", 99, false),
	LEGENDARY_PICKAXE("Легендарная кирка", 349, true),
	STEAM_PUNK_COLLECTOR("Стим-панк сборщик монет", 249, true),
	BOER("Глобальный бустер бура x2", 79, false),
	BIG_BOER("Глобальный бустер бура х5", 199,false),
	MUSEUM_ADVERTISEMENT("Реклама музея", 149, false),
	;

	private final String name;
	private final int price;
	private final boolean save; // можно ли купить повторно(возвращается в юзердате потом)

}
