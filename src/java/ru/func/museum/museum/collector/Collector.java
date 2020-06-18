package ru.func.museum.museum.collector;

import delfikpro.exhibit.Piece;
import lombok.experimental.Delegate;
import lombok.val;
import net.minecraft.server.v1_12_R1.*;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_12_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;
import ru.func.museum.data.collector.CollectorInfo;
import ru.func.museum.excavation.Excavation;
import ru.func.museum.museum.Museum;
import ru.func.museum.player.User;
import ru.func.museum.player.pickaxe.Pickaxe;

import java.util.ArrayList;
import java.util.List;

public class Collector {

	private final Museum museum;

	@Delegate
	private final CollectorInfo info;
	private CollectorNavigator navigator;
	private List<Piece> pieces;

	public Collector(Museum museum, CollectorInfo info) {
		this.museum = museum;
		this.info = info;
		this.navigator = museum.getPrototype().getDefaultCollectorNavigators().get(info.getId());
		this.pieces = new ArrayList<>();
		CraftWorld world = museum.getPrototype().getMap().getWorld();
		EntityArmorStand armorStand = new EntityArmorStand(world.getHandle());
		armorStand.setEquipment(EnumItemSlot.HEAD, CraftItemStack.asNMSCopy(new ItemStack(Material.WORKBENCH)));
		pieces.add(new Piece(armorStand, new Location(world, 0, 0, 0)));


	}


	public void show(User user) {
		val armorStand = new EntityArmorStand(Pickaxe.WORLD);

		navigator = new CollectorNavigator(Excavation.WORLD, endpoints);

		val location = getLocation(System.currentTimeMillis());

		armorStand.setCustomName("§6Коллектор " + collectorType.getName());
		armorStand.id = 800 + Pickaxe.RANDOM.nextInt(200);
		armorStand.setInvisible(true);
		armorStand.setCustomNameVisible(true);
		armorStand.setPosition(
				location.getX(),
				location.getY(),
				location.getZ()
							  );
		armorStand.setNoGravity(true);
		user.getConnection().sendPacket(new PacketPlayOutSpawnEntityLiving(armorStand));
		user.getConnection().sendPacket(new PacketPlayOutEntityEquipment(
				armorStand.id,
				EnumItemSlot.HEAD,
				CraftItemStack.asNMSCopy(collectorType.getHead())
		));
		this.armorStand = armorStand;
		previousLocation = location;
	}
	public void move(User user, long iteration) {

		val location = getLocation(iteration);

		user.getCoins().removeIf(coin -> coin.pickUp(player, user, location, collectorType.getRadius()));

		collectorType.move(
				((CraftPlayer) player).getHandle().playerConnection,
				armorStand,
				location.getX() - previousLocation.getX(),
				location.getY() - previousLocation.getY(),
				location.getZ() - previousLocation.getZ(),
				location.getYaw(),
				location.getPitch()
						  );
		previousLocation = location;
	}

	public void hide(User user) {
		connection.sendPacket(new PacketPlayOutEntityDestroy(armorStand.getId()));
	}
	private Location getLocation(long time) {
		return navigator.getLocation(time * collectorType.getSpeed() % 25_000 / 25_000D);
	}


}
