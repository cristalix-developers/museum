package ru.cristalix.museum.util;

public class Levels {

	public static long getRequiredExperience(int forLevel) {
		forLevel -= 1;
		return 100 * forLevel * forLevel - 50 * forLevel;
	}

	public static int getLevel(long experince) {
		return (int) (0.25 + 0.05 * Math.sqrt(4 * experince + 25)) + 1;
	}

	public static String formatExperience(long experience) {
		int level = getLevel(experience);
		return "§b" + level + " §7" + experience + "/" + getRequiredExperience(level + 1);

	}

}
