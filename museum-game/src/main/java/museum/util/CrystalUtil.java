package museum.util;

import lombok.experimental.UtilityClass;

/**
 * @author func 25.11.2020
 * @project museum
 */
@UtilityClass
public class CrystalUtil {

	public double convertCrystal2Money(long crystal) {
		return crystal * 4.1;
	}

	public int convertMoney2Cristal(double money) {
		return (int) (money / 4.1);
	}
}
