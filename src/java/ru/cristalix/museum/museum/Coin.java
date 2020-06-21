package ru.cristalix.museum.museum;

import clepto.bukkit.Lemonade;
import lombok.Getter;
import lombok.val;
import net.minecraft.server.v1_12_R1.*;
import org.bukkit.SoundCategory;
import org.bukkit.*;
import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack;
import ru.cristalix.museum.App;
import ru.cristalix.museum.player.User;
import ru.cristalix.museum.player.pickaxe.Pickaxe;

/**
 * @author func 08.06.2020
 * @project Museum
 */
public class Coin {

	public static final ItemStack COIN = CraftItemStack.asNMSCopy(Lemonade.get("coin").render());
	public static final int SECONDS_LIVE = 15;
	private EntityItem entityItem;
	private Location location;
	@Getter
	private long timestamp;

	public Coin(Location location) {
		this.location = location;
		entityItem = new EntityItem(App.getApp().getNMSWorld(), location.getX(), location.getY(), location.getZ(), COIN);
		entityItem.id = Pickaxe.RANDOM.nextInt(200) + 600;
		timestamp = System.currentTimeMillis();
	}

	public void remove(PlayerConnection connection) {
		connection.sendPacket(new PacketPlayOutEntityDestroy(entityItem.getId()));
	}

	public void create(PlayerConnection connection) {
		connection.sendPacket(new PacketPlayOutSpawnEntity(entityItem, 2));
		connection.sendPacket(new PacketPlayOutEntityMetadata(entityItem.getId(), entityItem.getDataWatcher(), false));
	}

	public boolean pickUp(User user, Location location, double radius) {
		boolean close = this.location.distanceSquared(location) <= radius * radius;

		if (close) {
			entityItem.setCustomNameVisible(true);
			entityItem.setNoGravity(true);

			// Расчет стоимости монеты
			val money = (user.getCurrentMuseum().getIncome() / user.getMuseums().size()) * (.5 + Pickaxe.RANDOM.nextDouble());
			val format = Math.floor(money * 100) / 100;
			entityItem.setCustomName("§6+ " + format + "$");

			val connection = user.getConnection();

			connection.sendPacket(new PacketPlayOutEntityVelocity(entityItem.getId(), 0, .05, 0));
			connection.sendPacket(new PacketPlayOutEntityMetadata(entityItem.getId(), entityItem.getDataWatcher(), false));

			user.setPickedCoinsCount(user.getPickedCoinsCount() + 1);

			Bukkit.getScheduler().runTaskLaterAsynchronously(App.getApp(), () -> {
				remove(connection);
				user.getPlayer().playSound(this.location, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.WEATHER, .2F, 1);
				user.getPlayer().spawnParticle(Particle.TOTEM, this.location.add(0, 1.5, 0), 5, 0, 0, 0, .3);
				user.setMoney(user.getMoney() + money);
			}, 30);
		}

		return close;
	}

}
