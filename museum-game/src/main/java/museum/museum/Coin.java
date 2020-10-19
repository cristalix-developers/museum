package museum.museum;

import clepto.bukkit.B;
import lombok.Getter;
import lombok.val;
import museum.App;
import museum.player.User;
import net.minecraft.server.v1_12_R1.*;
import org.bukkit.Location;

/**
 * @author func 08.06.2020
 * @project Museum
 */
public class Coin {

	public static final ItemStack COIN_ITEM = clepto.bukkit.item.Items.render("coin");
	public static final int SECONDS_LIVE = 20;
	private final EntityItem entityItem;
	@Getter
	private final long timestamp;

	public Coin(double x, double y, double z) {
		entityItem = new EntityItem(App.getApp().getNMSWorld(), x, y, z, COIN_ITEM);
		timestamp = System.currentTimeMillis();
	}

	public void remove(PlayerConnection connection) {
		connection.sendPacket(new PacketPlayOutEntityDestroy(entityItem.getId()));
	}

	public void create(PlayerConnection connection) {
		if (connection == null)
			return;
		connection.sendPacket(new PacketPlayOutSpawnEntity(entityItem, 2));
		connection.sendPacket(new PacketPlayOutEntityMetadata(entityItem.getId(), entityItem.getDataWatcher(), false));
	}

	public boolean pickUp(User user, Location location, double radius, int collectorId) {
		if (Math.abs(location.getX() - entityItem.getX()) > radius
				|| Math.abs(location.getY() - entityItem.getY()) > radius
				|| Math.abs(location.getZ() - entityItem.getZ()) > radius)
			return false;

		val connection = user.getConnection();

		connection.sendPacket(new PacketPlayOutCollect(entityItem.getId(), collectorId, 1));

		App app = App.getApp();

		val message = new EntityArmorStand(app.getNMSWorld(), entityItem.getX(), entityItem.getY(), entityItem.getZ());
		message.setCustomNameVisible(true);
		message.setMarker(true);
		message.setInvisible(true);
		message.setCustomName("§6+ " + user.getIncome() + "$");

		connection.sendPacket(new PacketPlayOutSpawnEntityLiving(message));
		connection.sendPacket(new PacketPlayOutEntity.PacketPlayOutRelEntityMove(message.getId(), 0, 3000, 0, false));

		user.setPickedCoinsCount(user.getPickedCoinsCount() + 1);
		user.setMoney(user.getMoney() + user.getIncome());

		B.postpone(30, () -> {
			remove(connection);
			connection.sendPacket(new PacketPlayOutEntityDestroy(message.getId()));
		});
		return true;
	}

}
