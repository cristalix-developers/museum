package ru.cristalix.museum.museum.map;

import lombok.Data;
import org.bukkit.Location;
import ru.cristalix.museum.museum.collector.CollectorNavigator;

import java.util.*;

@Data
public class MuseumPrototype {

	private final String address;
	private final MuseumManager map;
	private final Location origin;
	private final Location spawnPoint;
	private final List<CollectorNavigator> defaultCollectorNavigators = new ArrayList<>();
	private final List<SubjectPrototype> subjectPrototypes = new ArrayList<>();

	public MuseumPrototype(String address, MuseumManager map, Location origin) {
		this.address = address;
		this.map = map;
		this.origin = origin;
		this.spawnPoint = map.getLocations("spawn").stream().min(Comparator.comparingDouble(origin::distanceSquared))
				.orElseThrow(() -> new RuntimeException("No .p spawn found on the map."));

	}

}
