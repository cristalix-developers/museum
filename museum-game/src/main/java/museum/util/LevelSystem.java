package museum.util;

import lombok.experimental.UtilityClass;

@UtilityClass
public class LevelSystem {

	private final String LEVEL_FORMAT = "§b%d §7%d/%d";

	public long getRequiredExperience(int forLevel) {
		return forLevel * forLevel - forLevel / 2;
	}

	public int getLevel(long experience) {
		return (int) ((Math.sqrt(5) * Math.sqrt(experience * 80 + 5) + 5) / 20) + 1;
	}

	public String formatExperience(long experience) {
		int level = getLevel(experience);
		return String.format(LEVEL_FORMAT, level, experience, getRequiredExperience(level));
	}
}