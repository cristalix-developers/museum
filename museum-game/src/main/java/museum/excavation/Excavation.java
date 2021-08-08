package museum.excavation;

import clepto.bukkit.item.Items;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.val;
import museum.client_conversation.ModTransfer;
import museum.player.State;
import museum.player.User;
import museum.player.prepare.BeforePacketHandler;
import museum.util.ChunkWriter;
import museum.util.MessageUtil;
import museum.util.TreasureUtil;
import net.minecraft.server.v1_12_R1.BlockPosition;
import net.minecraft.server.v1_12_R1.PacketPlayOutMapChunk;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
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

		val pickaxe = Items.render(user.getPickaxeType().name().toLowerCase()).asBukkitMirror();
		val meta = pickaxe.getItemMeta();
		meta.addEnchant(Enchantment.DIG_SPEED, meta.getEnchantLevel(Enchantment.DIG_SPEED) + user.getInfo().getExtraSpeed(), true);
		pickaxe.setItemMeta(meta);
		inventory.addItem(pickaxe);

		inventory.addItem(prototype.getPallette());
		inventory.setItem(8, BeforePacketHandler.EMERGENCY_STOP);

		user.teleport(prototype.getSpawn().clone().add(0, 6, 0));
		user.getPlayer().sendTitle("§6Прибытие!", prototype.getTitle());

		MessageUtil.find("visitexcavation")
				.set("title", prototype.getTitle())
				.send(user);

		hitsLeft += user.getInfo().getExtraBreak();
	}

	@Override
	public void leaveState(User user) {
		TreasureUtil.sellAll(user);
	}

	@Override
	public boolean playerVisible() {
		return false;
	}

	@Override
	public boolean nightVision() {
		return true;
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

	public void updateHits(User user, int setHit) {
		hitsLeft = setHit;
		new ModTransfer()
				.integer(hitsLeft)
				.send("museum:hitcount", user);
	}
}
