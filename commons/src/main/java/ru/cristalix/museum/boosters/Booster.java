package ru.cristalix.museum.boosters;

import lombok.AllArgsConstructor;
import lombok.Getter;
import ru.cristalix.museum.utils.MultiTimeBar;
import ru.cristalix.museum.utils.UtilTime;

import java.util.UUID;

@AllArgsConstructor
@Getter
public class Booster implements MultiTimeBar.MultiBarInstance {

	private final UUID uniqueId;
	private final UUID user;
	private final String userName;
	private final BoosterType type;
	private final long until;
	private final long time;
	private final double multiplier;
	private final boolean global;

	public static Booster defaultInstance(UUID user, String userName, BoosterType type, long time, boolean global) {
		return new Booster(UUID.randomUUID(), user, userName, type, System.currentTimeMillis() + time, time, global ? type.getGlobalMultiplier() : type.getLocalMultiplier(), global);
	}

	@Override
	public double getPercentsOfFullTime() {
		return ((until - System.currentTimeMillis()) / ((double) time)) * 100.0;
	}

	@Override
	public String getTitle() {
		return "§eБустер §a" + getType().getName() + " §eот §b" + getType().getName() + " §a(" + UtilTime.formatTime(until - System.currentTimeMillis(), false) + ") §b/thx";
	}

	public boolean hadExpire() {
	    return System.currentTimeMillis() < until;
    }

}