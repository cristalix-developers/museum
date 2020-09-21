package museum.util;

import lombok.experimental.UtilityClass;

@UtilityClass
public class LevelSystem {

	private final String LEVEL_FORMAT = "§b%d §7%d/%d";

	public long getRequiredExperience(int forLevel) {
		return 100 * forLevel * forLevel - 50 * forLevel;
	}

	public int getLevel(long experience) {
		return (int) (0.25 + 0.05 * Math.sqrt(4 * experience + 25)) + 1;
	}

	public String formatExperience(long experience) {
		int level = getLevel(experience);
		return String.format(LEVEL_FORMAT, level, experience, getRequiredExperience(level));
	}

}
