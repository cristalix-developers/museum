package ru.cristalix.museum.museum.collector;

import ru.cristalix.museum.skeleton.Piece;
import lombok.experimental.Delegate;
import lombok.val;
import net.minecraft.server.v1_12_R1.EntityArmorStand;
import net.minecraft.server.v1_12_R1.EnumItemSlot;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_12_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;
import ru.cristalix.museum.Storable;
import ru.cristalix.museum.data.collector.CollectorInfo;
import ru.cristalix.museum.museum.Museum;
import ru.cristalix.museum.player.User;

import java.util.ArrayList;
import java.util.List;

public class Collector implements Storable<CollectorInfo> {

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

	@Override
	public CollectorInfo generateInfo() {
		return null;
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
