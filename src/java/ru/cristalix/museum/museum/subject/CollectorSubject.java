package ru.cristalix.museum.museum.subject;

import clepto.bukkit.Lemonade;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.server.v1_12_R1.EntityArmorStand;
import net.minecraft.server.v1_12_R1.EnumItemSlot;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;
import ru.cristalix.museum.App;
import ru.cristalix.museum.data.SubjectInfo;
import ru.cristalix.museum.museum.collector.CollectorNavigator;
import ru.cristalix.museum.museum.map.CollectorSubjectPrototype;
import ru.cristalix.museum.museum.map.SubjectPrototype;
import ru.cristalix.museum.museum.subject.skeleton.Piece;
import ru.cristalix.museum.player.User;

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
		this.piece = new Piece(armorStand, null);
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
		piece.show(user.getPlayer(), getLocation(System.currentTimeMillis()), false);
	}

	public void move(User user, long iteration) {
		if (navigator == null)
			return;
		Location location = getLocation(iteration);
		user.getCoins().removeIf(coin -> coin.pickUp(user, location, radius, piece.getEntityId()));
		piece.update(user.getPlayer(), location);
	}

	@Override
	public void hide(User user, boolean visually) {
		super.hide(user, visually);
		piece.hide(user.getPlayer());
	}

	private Location getLocation(long time) {
		int secondsPerLap = 20000 / speed;
		return navigator.getLocation(time % secondsPerLap / (double) secondsPerLap);
	}
}
