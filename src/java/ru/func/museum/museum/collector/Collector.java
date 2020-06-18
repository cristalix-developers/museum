package ru.func.museum.museum.collector;

import delfikpro.exhibit.Piece;
import lombok.experimental.Delegate;
import lombok.val;
import net.minecraft.server.v1_12_R1.*;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_12_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;
import ru.func.museum.data.collector.CollectorInfo;
import ru.func.museum.museum.Museum;
import ru.func.museum.player.User;

import java.util.ArrayList;
import java.util.List;

public class Collector {

	@Delegate
	private final CollectorInfo info;
	private final CollectorNavigator navigator;
	private final List<Piece> pieces = new ArrayList<>();

	public Collector(Museum museum, CollectorInfo info) {
		this.info = info;
		this.navigator = museum.getPrototype().getDefaultCollectorNavigators().get(info.getId());
		CraftWorld world = museum.getPrototype().getMap().getWorld();
		EntityArmorStand armorStand = new EntityArmorStand(world.getHandle());
		armorStand.setEquipment(EnumItemSlot.HEAD, CraftItemStack.asNMSCopy(new ItemStack(Material.WORKBENCH)));
		armorStand.setCustomName("§6Коллектор " + getType());
		armorStand.setCustomNameVisible(true);
		armorStand.setInvisible(true);
		armorStand.setNoGravity(true);

		pieces.add(new Piece(armorStand, new Location(world, 0, 0, 0)));
	}


	public void show(User user) {
		val location = getLocation(System.currentTimeMillis());
		for (Piece piece : pieces) piece.show(user.getPlayer(), location);
	}

	public void move(User user, long iteration) {

		val location = getLocation(iteration);

		user.getCoins().removeIf(coin -> coin.pickUp(user, location, getType().getRadius()));

		for (Piece piece : pieces) piece.update(user.getPlayer(), location);
	}

	public void hide(User user) {
		for (Piece piece : pieces) piece.hide(user.getPlayer());
	}
	private Location getLocation(long time) {
		int secondsPerLap = getType().getSecondsPerLap();
		return navigator.getLocation(time % secondsPerLap / (double) secondsPerLap);
	}


}
