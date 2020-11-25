package museum.international;

import clepto.ListUtils;
import clepto.bukkit.item.Items;
import clepto.cristalix.WorldMeta;
import lombok.val;
import museum.App;
import museum.player.User;
import museum.player.prepare.BeforePacketHandler;
import museum.util.MapLoader;
import net.minecraft.server.v1_12_R1.BlockPosition;
import net.minecraft.server.v1_12_R1.PacketPlayInBlockDig;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import ru.cristalix.core.scoreboard.SimpleBoardObjective;

import java.util.List;
import java.util.stream.Collectors;

import static net.minecraft.server.v1_12_R1.PacketPlayInBlockDig.EnumPlayerDigType.ABORT_DESTROY_BLOCK;

/**
 * @author func 25.11.2020
 * @project museum
 */
public class CrystalExcavation implements International {
	private final WorldMeta worldMeta;
	private final List<Location> spawnPoints;
	private final ItemStack crystal = Items.render("crystal").asBukkitMirror();

	public CrystalExcavation() {
		this.worldMeta = MapLoader.load("taiga");
		this.spawnPoints = worldMeta.getLabels("spawn").stream()
				.map(label -> label.add(0, 2, 0))
				.collect(Collectors.toList());
	}

	@Override
	public void setupScoreboard(User user, SimpleBoardObjective obj) {
		obj.setDisplayName("Международные раскопки");
	}

	@Override
	public void enterState(User user) {
		val player = user.getPlayer();
		player.setAllowFlight(false);
		player.setFlying(false);
		user.teleport(ListUtils.random(spawnPoints));
		val inventory = user.getInventory();
		inventory.clear();
		// Предметы кешируются
		inventory.addItem(Items.render(user.getPickaxeType().name().toLowerCase()).asBukkitMirror());
		inventory.addItem(new ItemStack(Material.FISHING_ROD));
		inventory.setItem(8, BeforePacketHandler.EMERGENCY_STOP);

		val app = App.getApp();
		for (User current : app.getUsers()) {
			if (current.getState() instanceof CrystalExcavation) {
				current.getPlayer().showPlayer(app, user.getPlayer());
				user.getPlayer().showPlayer(app, current.getPlayer());
			}
		}
	}

	@Override
	public void leaveState(User user) {
		val app = App.getApp();
		for (User current : app.getUsers()) {
			if (current.getState() instanceof CrystalExcavation)
				continue;
			current.getPlayer().hidePlayer(app, user.getPlayer());
			user.getPlayer().hidePlayer(app, current.getPlayer());
		}
	}

	@Override
	public void acceptBlockBreak(User user, PacketPlayInBlockDig packet) {
		// Не выносить canBeBroken(packet.a)) | Операция тяжелее
		if (packet.c == PacketPlayInBlockDig.EnumPlayerDigType.STOP_DESTROY_BLOCK) {
			if (canBeBroken(packet.a)) {
				user.getInventory().addItem(crystal);
				user.setCrystal(user.getCrystal() + 1);
			}
		} else if (packet.c == PacketPlayInBlockDig.EnumPlayerDigType.START_DESTROY_BLOCK) {
			if (!canBeBroken(packet.a)) {
				packet.c = ABORT_DESTROY_BLOCK;
				packet.a = BeforePacketHandler.DUMMY;
			}
		}
	}

	@Override
	public boolean canBeBroken(BlockPosition pos) {
		return worldMeta.getWorld().getBlockAt(pos.getX(), pos.getY(), pos.getZ()).getType() == Material.STAINED_GLASS;
	}
}
