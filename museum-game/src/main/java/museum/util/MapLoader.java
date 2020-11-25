package museum.util;

import clepto.cristalix.Cristalix;
import clepto.cristalix.WorldMeta;
import lombok.experimental.UtilityClass;
import lombok.val;
import museum.App;
import museum.player.State;
import net.minecraft.server.v1_12_R1.PacketPlayOutMapChunk;
import net.minecraft.server.v1_12_R1.World;
import ru.cristalix.core.map.BukkitWorldLoader;
import ru.cristalix.core.map.MapListDataItem;

import java.util.concurrent.ExecutionException;

/**
 * @author func 13.09.2020
 * @project museum
 */
@UtilityClass
public class MapLoader {

	public WorldMeta load(String map) {
		// Загрузка карты с сервера BUIL-1
		MapListDataItem mapInfo = Cristalix.mapService().getLatestMapByGameTypeAndMapName("Museum", map)
				.orElseThrow(() -> new RuntimeException("Map Museum/release wasn't found in the MapService"));

		WorldMeta meta;
		try {
			meta = new WorldMeta(Cristalix.mapService().loadMap(mapInfo.getLatest(), BukkitWorldLoader.INSTANCE).get());
		} catch (InterruptedException | ExecutionException exception) {
			exception.printStackTrace();
			Thread.currentThread().interrupt();
			return null;
		}

		val world = meta.getWorld();
		world.setGameRuleValue("mobGriefing", "false");
		world.setGameRuleValue("doTileDrops", "false");
		return meta;
	}

	public void interceptChunkWriter(App app, World world) {
		// Инжектим блоки в чанки (patched paper)
		world.chunkInterceptor = (chunk, flags, receiver) -> {
			val user = app.getUser(receiver.getUniqueID());
			if (user == null)
				return new PacketPlayOutMapChunk(chunk, flags);
			State state = user.getState();
			if (state == null)
				return new PacketPlayOutMapChunk(chunk, flags);
			val worldChunk = world.getChunkAt(chunk.locX, chunk.locZ);
			ChunkWriter chunkWriter = new ChunkWriter(worldChunk);
			state.rewriteChunk(user, chunkWriter);
			return chunkWriter.build(flags);
		};
	}
}
