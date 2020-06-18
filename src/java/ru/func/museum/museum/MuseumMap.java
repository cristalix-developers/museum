package ru.func.museum.museum;

import clepto.cristalix.Cristalix;
import clepto.cristalix.WorldMeta;
import lombok.Getter;
import org.bukkit.World;
import ru.cristalix.core.map.BukkitWorldLoader;
import ru.cristalix.core.map.LoadedMap;
import ru.cristalix.core.map.MapListDataItem;
import ru.func.museum.App;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

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



	}

	public MuseumPrototype getPrototype(String address) {
		return prototypeMap.get(address);
	}



}
