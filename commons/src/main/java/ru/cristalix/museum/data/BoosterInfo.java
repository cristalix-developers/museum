package ru.cristalix.museum.data;

import lombok.Data;
import ru.cristalix.museum.boosters.BoosterType;

import java.util.UUID;

@Data
public class BoosterInfo implements Unique {

	private final UUID uuid;
	private final UUID owner;
	private final String ownerName;
	private final BoosterType type;
	private final long until;
	private final double multiplier;
	private final boolean global;

}
