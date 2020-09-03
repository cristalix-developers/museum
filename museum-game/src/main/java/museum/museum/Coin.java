package museum.museum;

import clepto.bukkit.Lemonade;
import lombok.Getter;
import lombok.val;
import net.minecraft.server.v1_12_R1.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack;
import museum.App;
import museum.player.User;
import museum.player.pickaxe.Pickaxe;

/**
 * @author func 08.06.2020
 * @project Museum
 */
public class Coin {

	public static final ItemStack COIN = CraftItemStack.asNMSCopy(Lemonade.get("coin").render());
	public static final int SECONDS_LIVE = 20;
	private EntityItem entityItem;
	private Location location;
	@Getter
	private long timestamp;

	public Coin(Location location) {
		this.location = location;
		entityItem = new EntityItem(App.getApp().getNMSWorld(), location.getX(), location.getY(), location.getZ(), COIN);
		timestamp = System.currentTimeMillis();
	}

	public void remove(PlayerConnection connection) {
		connection.sendPacket(new PacketPlayOutEntityDestroy(entityItem.getId()));
	}

	public void create(PlayerConnection connection) {
		connection.sendPacket(new PacketPlayOutSpawnEntity(entityItem, 2));
		connection.sendPacket(new PacketPlayOutEntityMetadata(entityItem.getId(), entityItem.getDataWatcher(), false));
	}

	public boolean pickUp(User user, Location location, double radius, int collectorId) {
		boolean close = this.location.distanceSquared(location) <= radius * radius;

		if (close) {
			// Расчет стоимости монеты
			val money = (user.getCurrentMuseum().getIncome() / user.getMuseums().size()) * (.5 + Pickaxe.RANDOM.nextDouble());
			val format = Math.floor(money * 100) / 100;

			val connection = user.getConnection();

			connection.sendPacket(new PacketPlayOutCollect(entityItem.getId(), collectorId, 1));

			App app = App.getApp();

			val message = new EntityArmorStand(app.getNMSWorld(), entityItem.getX(), entityItem.getY(), entityItem.getZ());
			message.setCustomNameVisible(true);
			message.setMarker(true);
			message.setInvisible(true);
			message.setCustomName("§6+ " + format + "$");

			connection.sendPacket(new PacketPlayOutSpawnEntityLiving(message));
			connection.sendPacket(new PacketPlayOutEntity.PacketPlayOutRelEntityMove(message.getId(), 0, 3000, 0, false));

			user.setPickedCoinsCount(user.getPickedCoinsCount() + 1);
			user.setMoney(user.getMoney() + money);

			Bukkit.getScheduler().runTaskLaterAsynchronously(app, () -> {
				remove(connection);
				connection.sendPacket(new PacketPlayOutEntityDestroy(message.getId()));
			}, 30);
		}
		return close;
	}

}
