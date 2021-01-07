package museum.util;

import lombok.experimental.UtilityClass;
import museum.player.User;

/**
 * @author func 25.11.2020
 * @project museum
 */
@UtilityClass
public class CrystalUtil {

	public double convertCrystal2Money(User user, long crystal) {
		// Бонус при обмене кристаллов на деньги
		int multiplier = 1;
		if (user.getPrefix() != null && user.getPrefix().equals("㕐"))
			multiplier = 2;
		return crystal * 4.1 * multiplier;
	}

	public int convertMoney2Crystal(double money) {
		return (int) (money / 4.1);
	}
}
