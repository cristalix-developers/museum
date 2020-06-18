package ru.func.museum.museum.map;

import clepto.cristalix.Cristalix;
import clepto.cristalix.WorldMeta;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.util.Vector;
import ru.cristalix.core.build.models.IZone;
import ru.cristalix.core.build.models.Point;
import ru.cristalix.core.map.BukkitWorldLoader;
import ru.cristalix.core.map.LoadedMap;
import ru.cristalix.core.map.MapListDataItem;
import ru.func.museum.App;
import ru.func.museum.museum.collector.CollectorNavigator;
import ru.func.museum.museum.hall.template.space.SkeletonSpaceViewer;
import ru.func.museum.museum.hall.template.space.Space;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Getter
public class MuseumMap implements WorldMeta {

	private final App app;
	private final LoadedMap<World> cristalixMap;
	private final Map<String, MuseumPrototype> prototypeMap = new HashMap<>();

	public MuseumMap(App app) {
		this.app = app;
		MapListDataItem mapInfo = Cristalix.mapService().getMapByGameTypeAndMapName("museum", "main")
				.orElseThrow(() -> new RuntimeException("Map museum/main wasn't found in the MapService"));

		try {
			this.cristalixMap = Cristalix.mapService().loadMap(mapInfo.getLatest(), BukkitWorldLoader.INSTANCE).get();
		} catch (InterruptedException | ExecutionException e) {
			throw new RuntimeException(e);
		}

		for (Point museumPoint : getPoints("museum")) {
			MuseumPrototype prototype = new MuseumPrototype(museumPoint.getTag(), this, point2Loc(museumPoint));
			prototypeMap.put(prototype.getAddress(), prototype);
		}

		Map<String, CollectorNavigator> routes = new HashMap<>();
		for (Map.Entry<String, List<Point>> e : getPoints().entrySet()) {
			String key = e.getKey();
			if (!key.startsWith("path")) continue;
			List<Location> points = e.getValue().stream()
					.sorted(Comparator.comparing(Point::getTag))
					.map(this::point2Loc)
					.collect(Collectors.toList());
			Location avg = new Location(getWorld(), 0, 0, 0);
			points.forEach(avg::add);
			Location average = avg.toVector().multiply(1.0 / points.size()).toLocation(getWorld());
			MuseumPrototype prototype = prototypeMap.values().stream()
					.min(Comparator.comparingDouble(proto -> average.distanceSquared(proto.getOrigin())))
					.orElseThrow(() -> new RuntimeException("No museum prototypes found!"));
			routes.put(key, new CollectorNavigator(prototype, getWorld(), points));
		}
		routes.entrySet().stream()
				.sorted(Map.Entry.comparingByKey())
				.map(Map.Entry::getValue)
				.forEach(navigator -> navigator.getMuseumProto().getDefaultCollectorNavigators().add(navigator));

		for (Point point : getPoints("space")) {
			Location location = point2Loc(point);
			String[] ss = point.getTag().split(" ");

			MuseumPrototype prototype = prototypeMap.values().stream()
					.min(Comparator.comparingDouble(o -> location.distanceSquared(o.getOrigin())))
					.orElseThrow(() -> new RuntimeException("No museum prototypes found!"));

			Supplier<Space> spaceSupplier;
			if ("skeleton".startsWith(ss[0].toLowerCase())) spaceSupplier = () -> new SkeletonSpaceViewer();

			SpacePrototype spacePrototype = new SpacePrototype(location);
			prototype.getSpacePrototypes().add(spacePrototype);
		}

	}

	public MuseumPrototype getPrototype(String address) {
		return prototypeMap.get(address);
	}



}
