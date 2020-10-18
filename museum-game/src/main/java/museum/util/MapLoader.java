package museum.util;

import clepto.cristalix.Cristalix;
import clepto.cristalix.WorldMeta;
import lombok.experimental.UtilityClass;
import lombok.val;
import museum.App;
import museum.player.State;
import net.minecraft.server.v1_12_R1.PacketPlayOutMapChunk;
import ru.cristalix.core.map.BukkitWorldLoader;
import ru.cristalix.core.map.MapListDataItem;

import java.util.concurrent.ExecutionException;

/**
 * @author func 13.09.2020
 * @project museum
 */
@UtilityClass
public class MapLoader {

	public void load(App app) {
		// Загрузка карты с сервера BUIL-1
		MapListDataItem mapInfo = Cristalix.mapService().getMapByGameTypeAndMapName("Museum", "release")
				.orElseThrow(() -> new RuntimeException("Map Museum/release wasn't found in the MapService"));

		try {
			app.setMap(new WorldMeta(Cristalix.mapService().loadMap(mapInfo.getLatest(), BukkitWorldLoader.INSTANCE).get()));
		} catch (InterruptedException | ExecutionException exception) {
			exception.printStackTrace();
			Thread.currentThread().interrupt();
		}

		val world = app.getWorld();
		world.setGameRuleValue("mobGriefing", "false");
		world.setGameRuleValue("doTileDrops", "false");

		// Инжектим блоки в чанки (patched paper)
		app.getNMSWorld().chunkInterceptor = (chunk, flags, receiver) -> {
			val user = app.getUser(receiver.getUniqueID());
			State state = user.getState();
			if (state == null)
				return new PacketPlayOutMapChunk(chunk, flags);
			val worldChunk = app.getNMSWorld().getChunkAt(chunk.locX, chunk.locZ);
			ChunkWriter chunkWriter = new ChunkWriter(worldChunk);
			state.rewriteChunk(user, chunkWriter);
			return chunkWriter.build(flags);
		};
	}

}
