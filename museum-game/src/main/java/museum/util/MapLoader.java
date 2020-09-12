package museum.util;

import clepto.bukkit.B;
import clepto.cristalix.Cristalix;
import clepto.cristalix.mapservice.WorldMeta;
import lombok.experimental.UtilityClass;
import lombok.val;
import museum.App;
import museum.museum.subject.CollectorSubject;
import museum.museum.subject.Subject;
import museum.player.User;
import museum.player.pickaxe.Pickaxe;
import museum.prototype.Managers;
import net.minecraft.server.v1_12_R1.Chunk;
import net.minecraft.server.v1_12_R1.EntityPlayer;
import net.minecraft.server.v1_12_R1.PacketPlayOutMapChunk;
import ru.cristalix.core.map.BukkitWorldLoader;
import ru.cristalix.core.map.MapListDataItem;

import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

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
		} catch (InterruptedException | ExecutionException e) {
			throw new RuntimeException(e);
		}
		app.getMap().getWorld().setGameRuleValue("mobGriefing", "false");
		// todo: отсылать мобов тоже через перепись чанка
		// Инжектим блоки в чанки (patched paper)
		app.getNMSWorld().chunkInterceptor = (Chunk chunk, int flags, EntityPlayer receiver) -> {
			val user = app.getUser(receiver.getUniqueID());

			if (user.getExcavation() != null)
				return new PacketPlayOutMapChunk(chunk, flags);
			val worldChunk = app.getNMSWorld().getChunkAt(chunk.locX, chunk.locZ);
			rewriteChunk(user, worldChunk, false, (subject -> B.postpone(1, () -> {
				if (subject instanceof CollectorSubject) {
					val loc = ((CollectorSubject) subject).getCollectorLocation();
					if (loc.getX() / 16 == worldChunk.locX && loc.getZ() / 16 == worldChunk.locZ) {
						val piece = ((CollectorSubject) subject).getPiece();
						piece.hide(user.getPlayer());
						piece.show(user.getPlayer(), loc);
					}
				}
			})));
			val packet = new PacketPlayOutMapChunk(worldChunk, flags);
			rewriteChunk(user, worldChunk, true, null);
			return packet;
		};
	}

	private void rewriteChunk(User user, Chunk chunk, boolean goBack, Consumer<Subject> doWithSubject) {
		for (Subject subject : user.getMuseums().get(Managers.museum.getPrototype("main")).getSubjects()) {
			val allocation = subject.getAllocation();
			if (allocation == null)
				continue;

			allocation.getBlocks().forEach((position, data) -> {
				if (position.getX() / 16 == chunk.locX && position.getZ() / 16 == chunk.locZ)
					chunk.a(position, goBack ? Pickaxe.AIR_DATA : data);
			});

			if (doWithSubject == null)
				continue;
			doWithSubject.accept(subject);
		}
	}
}
