package ru.cristalix.museum.boosters;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

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

}