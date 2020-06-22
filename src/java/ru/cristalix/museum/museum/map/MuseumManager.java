package ru.cristalix.museum.museum.map;

import clepto.cristalix.*;
import lombok.Getter;
import lombok.experimental.Delegate;
import org.bukkit.Location;
import org.bukkit.World;
import ru.cristalix.core.map.BukkitWorldLoader;
import ru.cristalix.core.map.LoadedMap;
import ru.cristalix.core.map.MapListDataItem;
import ru.cristalix.museum.App;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Getter
public class MuseumManager {

	private final App app;

	@Delegate
	private final WorldMeta worldMeta;

	private final Map<String, MuseumPrototype> museumPrototypeMap = new HashMap<>();
	private final Map<String, SubjectPrototype> subjectPrototypeMap = new HashMap<>();

	public MuseumManager(App app) {
		this.app = app;
		MapListDataItem mapInfo = Cristalix.mapService().getMapByGameTypeAndMapName("MODELS", "Dino")
				.orElseThrow(() -> new RuntimeException("Map museum/main wasn't found in the MapService"));

		LoadedMap<World> cristalixMap;
		try {
			cristalixMap = Cristalix.mapService().loadMap(mapInfo.getLatest(), BukkitWorldLoader.INSTANCE).get();
		} catch (InterruptedException | ExecutionException e) {
			throw new RuntimeException(e);
		}

		this.worldMeta = new WorldMeta(cristalixMap);

		this.worldMeta.getBoxes("subject").forEach((address, box) -> {
			String typeStr = box.requireLabel("type").getTag();
			double price = box.requireLabel("price").getTagDouble();
			List<Label> manipulators = box.getLabels("manipulator");
			Location origin = box.getLabels("origin").stream().findAny().orElse(null);
			if (origin == null) origin = box.getCenter();

			SubjectType type = SubjectType.byString(typeStr);
			if (type == null) throw new MapServiceException("Subject type '" + typeStr + "' is not a valid type.");
			SubjectPrototype prototype = new SubjectPrototype(address, type, price, box,
					box.toRelativeVector(origin),
					manipulators.stream().map(box::toRelativeVector).collect(Collectors.toList())
			);

			subjectPrototypeMap.put(address, prototype);
		});

		for (Map.Entry<String, Box> e : worldMeta.getBoxes("museum").entrySet()) {
			String address = e.getKey();

			Box box = e.getValue();

			MuseumPrototype prototype = new MuseumPrototype(address, this,
					box.requireLabel("spawn"),
					box.getLabels("default").stream()
							.map(label -> subjectPrototypeMap.get(label.getTag()))
							.collect(Collectors.toList())
			);

			museumPrototypeMap.put(address, prototype);
		}

		getWorld().setGameRuleValue("mobGriefing", "false");

	}

	public MuseumPrototype getMuseumPrototype(String address) {
		return museumPrototypeMap.get(address);
	}

	public SubjectPrototype getSubjectPrototype(String address) {
		return subjectPrototypeMap.get(address);
	}

}
