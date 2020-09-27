package museum.excavation;

import clepto.bukkit.item.Items;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.val;
import museum.player.State;
import museum.player.User;
import museum.player.prepare.BeforePacketHandler;
import museum.util.ChunkWriter;
import museum.util.MessageUtil;
import net.minecraft.server.v1_12_R1.BlockPosition;
import net.minecraft.server.v1_12_R1.PacketPlayOutMapChunk;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import ru.cristalix.core.scoreboard.SimpleBoardObjective;

@Data
@AllArgsConstructor
public class Excavation implements State {

	private final ExcavationPrototype prototype;
	private int hitsLeft;

	public static boolean isAir(User user, BlockPosition pos) {
		return user.getWorld().getBlockAt(pos.getX(), pos.getY(), pos.getZ()).getType() == Material.AIR;
	}

	@SuppressWarnings("deprecation")
	@Override
	public void enterState(User user) {
		Player player = user.getPlayer();
		player.setAllowFlight(false);

		val inventory = player.getInventory();

		inventory.clear();
		inventory.addItem(Items.render(user.getPickaxeType().name().toLowerCase()).asBukkitMirror());
		inventory.setItem(8, BeforePacketHandler.EMERGENCY_STOP);

		for (val item : prototype.getPallette())
			inventory.addItem(item);

		user.teleport(prototype.getSpawn());
		user.getPlayer().sendTitle("§6Прибытие!", prototype.getTitle());

		MessageUtil.find("visitexcavation")
				.set("title", prototype.getTitle())
				.send(user);
	}

	@Override
	public void leaveState(User user) {

	}

	@Override
	public void rewriteChunk(User user, ChunkWriter chunkWriter) {
		for (PacketPlayOutMapChunk packet : prototype.getPackets()) {
			if (packet.a == chunkWriter.getChunk().locX && packet.b == chunkWriter.getChunk().locZ) {
				chunkWriter.setReadyPacket(packet);
				return;
			}
		}
	}

	@Override
	public void setupScoreboard(User user, SimpleBoardObjective objective) {
		objective.setDisplayName("Раскопки");
		objective.startGroup("Раскопки")
				.record("Ударов", () -> Math.max(hitsLeft, 0) + " осталось")
				.record("Шахта", prototype.getTitle());
	}
}
