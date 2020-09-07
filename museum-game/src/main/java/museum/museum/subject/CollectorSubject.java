package museum.museum.subject;

import clepto.bukkit.Lemonade;
import lombok.Getter;
import lombok.Setter;
import museum.museum.subject.skeleton.Piece;
import museum.museum.subject.skeleton.V4;
import net.minecraft.server.v1_12_R1.EntityArmorStand;
import net.minecraft.server.v1_12_R1.EnumItemSlot;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;
import museum.App;
import museum.data.SubjectInfo;
import museum.museum.collector.CollectorNavigator;
import museum.museum.map.CollectorSubjectPrototype;
import museum.museum.map.SubjectPrototype;
import museum.player.User;

public class CollectorSubject extends Subject {

	@Getter
	private final int id;
	private final Piece piece;
	@Setter
	private CollectorNavigator navigator;

	private final double radius;
	private final int speed;

	public CollectorSubject(SubjectPrototype prototype, SubjectInfo info, User owner) {
		super(prototype, info, owner);
		EntityArmorStand armorStand = new EntityArmorStand(App.getApp().getNMSWorld());
		Lemonade lemonade = Lemonade.get(this.prototype.getAddress());
		ItemStack item = lemonade == null ? new ItemStack(Material.WORKBENCH) : lemonade.render();
		armorStand.setSlot(EnumItemSlot.HEAD, CraftItemStack.asNMSCopy(item));
		armorStand.setCustomName(prototype.getTitle());
		armorStand.setCustomNameVisible(true);
		this.navigator = null;
		this.piece = new Piece(armorStand);
		this.id = info.getMetadata() == null ? 0 : Integer.parseInt(info.getMetadata());
		this.speed = (int) ((CollectorSubjectPrototype) prototype).getSpeed();
		this.radius = ((CollectorSubjectPrototype) prototype).getRadius();
	}

	@Override
	public void updateInfo() {
		cachedInfo.metadata = String.valueOf(id);
	}

	@Override
	public void show(User user) {
		super.show(user);
		piece.show(user.getPlayer(), V4.fromLocation(getLocation(System.currentTimeMillis())));
	}

	public void move(User user, long iteration) {
		if (navigator == null)
			return;
		Location location = getLocation(iteration);
		user.getCoins().removeIf(coin -> coin.pickUp(user, location, radius, piece.getStand().id));
		piece.update(user.getPlayer(), location);
	}

	@Override
	public void hide(User user) {
		super.hide(user);
		piece.hide(user.getPlayer());
	}

	private Location getLocation(long time) {
		int secondsPerLap = 20000 / speed;
		return navigator.getLocation(time % secondsPerLap / (double) secondsPerLap);
	}
}
