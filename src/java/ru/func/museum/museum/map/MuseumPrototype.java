package ru.func.museum.museum.map;

import lombok.Data;
import org.bukkit.Location;
import ru.cristalix.core.build.models.Point;
import ru.func.museum.museum.collector.CollectorNavigator;

import java.util.*;
import java.util.stream.Collectors;

@Data
public class MuseumPrototype {

	private final String address;
	private final MuseumMap map;
	private final Location origin;
	private final Location spawnPoint;
	private final List<CollectorNavigator> defaultCollectorNavigators = new ArrayList<>();
	private final List<SpacePrototype> spacePrototypes = new ArrayList<>();

	public MuseumPrototype(String address, MuseumMap map, Location origin) {
		this.address = address;
		this.map = map;
		this.origin = origin;
		this.spawnPoint = map.getLocations("spawn").stream().min(Comparator.comparingDouble(origin::distanceSquared))
				.orElseThrow(() -> new RuntimeException("No .p spawn found on the map."));

	}

}
