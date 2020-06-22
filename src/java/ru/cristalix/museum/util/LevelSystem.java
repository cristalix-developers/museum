package ru.cristalix.museum.util;

public class LevelSystem {

	public static long getRequiredExperience(int forLevel) {
		forLevel--;
		return 100 * forLevel * forLevel - 50 * forLevel;
	}

	public static int getLevel(long experience) {
		return (int) (0.25 + 0.05 * Math.sqrt(4 * experience + 25)) + 1;
	}

	public static String formatExperience(long experience) {
		int level = getLevel(experience);
		return "§b" + level + " §7" + experience + "/" + getRequiredExperience(++level);
	}

}
