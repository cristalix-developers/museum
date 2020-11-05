package museum.donate;

import java.util.EnumMap;
import java.util.Map;

import static museum.boosters.BoosterType.*;
import static museum.donate.DonateType.*;

public class Donate {

	private final Map<DonateType, DonateArchetype> donateMap = new EnumMap<>(DonateType.class);

	public Donate() {

		// Глобальные бустеры
		donateMap.put(GLOBAL_MONEY_BOOSTER, new GlobalBoosterArchetype(COINS));
		donateMap.put(GLOBAL_EXP_BOOSTER, new GlobalBoosterArchetype(EXP));
		donateMap.put(GLOBAL_VILLAGER_BOOSTER, new GlobalBoosterArchetype(VILLAGER));



	}

}
