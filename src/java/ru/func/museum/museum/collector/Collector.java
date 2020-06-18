package ru.func.museum.museum.collector;

import lombok.experimental.Delegate;
import lombok.val;
import net.minecraft.server.v1_12_R1.*;
import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack;
import ru.func.museum.data.collector.CollectorInfo;
import ru.func.museum.excavation.Excavation;
import ru.func.museum.player.User;
import ru.func.museum.player.pickaxe.Pickaxe;

public class Collector {

	@Delegate
	private final CollectorInfo info;

	public Collector(CollectorInfo info) {
		this.info = info;
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

	public void hide(User user) {
	}

}
