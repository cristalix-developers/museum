package ru.cristalix.museum.boosters;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@AllArgsConstructor
@Getter
public class Booster {

	private final UUID uniqueId;
	private final UUID user;
	private final String userName;
	private final BoosterType type;
	private final long until;
	private final double multiplier;
	private final boolean global;

	public static Booster defaultInstance(UUID user, String userName, BoosterType type, boolean global) {
		return new Booster(UUID.randomUUID(), user, userName, type, System.currentTimeMillis() + TimeUnit.HOURS.toMillis(1L), global ? type.getGlobalMultiplier() : type.getLocalMultiplier(), global);
	}

}