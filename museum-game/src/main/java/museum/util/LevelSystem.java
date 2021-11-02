package museum.util;

import lombok.experimental.UtilityClass;
import lombok.val;
import museum.player.User;

@UtilityClass
public class LevelSystem {

	public long getRequiredExperience(int forLevel) {
		return forLevel * forLevel - forLevel / 2;
	}

	public int getLevel(long experience) {
		return (int) ((Math.sqrt(5) * Math.sqrt(experience * 80 + 5) + 5) / 20) + 1;
	}

	public static boolean acceptGiveExp(User user, int excavationLvl) {
		val userLevel = user.getLevel();
		return Math.abs(userLevel - excavationLvl) < 65 || (userLevel > 210 && excavationLvl > 210) || (userLevel < 400 && excavationLvl > 149);
	}
}