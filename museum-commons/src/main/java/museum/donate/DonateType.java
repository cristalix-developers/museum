package museum.donate;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import museum.boosters.BoosterType;
import museum.data.model.CollectorModel;
import museum.data.model.PickaxeModel;
import museum.data.PickaxeType;
import museum.data.UserInfo;

import java.util.function.Predicate;

@Getter
@AllArgsConstructor
@RequiredArgsConstructor
public enum DonateType {

	GLOBAL_MONEY_BOOSTER("Глобальный бустер денег", 199),
	GLOBAL_VILLAGER_BOOSTER("Глобальный бустер посетителей", 149),
	GLOBAL_EXP_BOOSTER("Глобальный бустер опыта", 149),

	LOCAL_MONEY_BOOSTER("Локальный бустер денег", 99, userInfo -> userInfo.getLocalBoosters().stream()
			.anyMatch(booster -> booster.getType() == BoosterType.COINS)),

	LOCAL_EXP_BOOSTER("Локальный бустер опыта", 99, userInfo -> userInfo.getLocalBoosters().stream()
			.anyMatch(booster -> booster.getType() == BoosterType.COINS)),

	LEGENDARY_PICKAXE("Легендарная кирка", 349, userInfo -> userInfo.getModels().stream()
			.anyMatch(item -> item instanceof PickaxeModel && ((PickaxeModel) item).getType() == PickaxeType.LEGENDARY)),

	STEAM_PUNK_COLLECTOR("Стим-панк сборщик монет", 249, userInfo -> userInfo.getModels().stream()
			.anyMatch(item -> item instanceof CollectorModel && item.getAddress().equals("steampunk")));

	private final String name;
	private final int price;

	// Проверялка того, есть ли уже данный донат у юзера
	private Predicate<UserInfo> preventionTest;

}
