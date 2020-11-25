package museum.international;

import clepto.ListUtils;
import clepto.bukkit.groovy.Do;
import clepto.bukkit.item.Items;
import clepto.cristalix.WorldMeta;
import lombok.val;
import museum.player.User;
import museum.player.prepare.BeforePacketHandler;
import museum.util.MapLoader;
import museum.util.MessageUtil;
import net.minecraft.server.v1_12_R1.BlockPosition;
import net.minecraft.server.v1_12_R1.PacketPlayInBlockDig;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
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
	private WorldMeta worldMeta;
	private WorldMeta tempWorld;
	private List<Location> spawnPoints;
	private final ItemStack crystal = Items.render("crystal").asBukkitMirror();
	private final ItemStack hook = Items.render("hook").asBukkitMirror();

	public CrystalExcavation() {
		load();
		worldMeta = tempWorld;
		// Каждые 100 минут, выгружать игроков в третий мир и отгружать второй мир, затем третий мир становится вторым
		Do.every(100).minutes(() -> {
			load();
			for (Player player : worldMeta.getWorld().getPlayers()) {
				val location = player.getLocation();
				location.setWorld(tempWorld.getWorld());
				player.teleport(location);
			}
			Bukkit.unloadWorld(worldMeta.getWorld(), false);
			worldMeta = tempWorld;
		});
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
		inventory.addItem(Items.render(user.getPickaxeType().name().toLowerCase()).asBukkitMirror(), hook);
		inventory.setItem(8, BeforePacketHandler.EMERGENCY_STOP);

		user.getPlayer().sendTitle("Прибытие!", "§bдобывайте кристаллы");
	}

	@Override
	public void leaveState(User user) {
		MessageUtil.find("leave-crystal").send(user);
	}

	@Override
	public boolean playerVisible() {
		return true;
	}

	@Override
	public boolean nightVision() {
		return false;
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

	private void load() {
		tempWorld = MapLoader.load("taiga");
		spawnPoints = tempWorld.getLabels("spawn").stream()
				.map(label -> label.add(0, 2, 0))
				.collect(Collectors.toList());
	}
}