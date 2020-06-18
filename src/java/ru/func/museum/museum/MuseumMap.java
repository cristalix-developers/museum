package ru.func.museum.museum;

import clepto.cristalix.Cristalix;
import clepto.cristalix.WorldMeta;
import lombok.Getter;
import org.bukkit.World;
import ru.cristalix.core.map.BukkitWorldLoader;
import ru.cristalix.core.map.LoadedMap;
import ru.cristalix.core.map.MapListDataItem;

import java.util.concurrent.ExecutionException;

@Getter
public class MuseumMap implements WorldMeta {

	private final LoadedMap<World> cristalixMap;

	public MuseumMap() {
		MapListDataItem mapInfo = Cristalix.mapService().getMapByGameTypeAndMapName("museum", "main")
				.orElseThrow(() -> new RuntimeException("Map museum/main wasn't found in the MapService"));

		try {
			this.cristalixMap = Cristalix.mapService().loadMap(mapInfo.getLatest(), BukkitWorldLoader.INSTANCE).get();
		} catch (InterruptedException | ExecutionException e) {
			throw new RuntimeException(e);
		}
	}
}
