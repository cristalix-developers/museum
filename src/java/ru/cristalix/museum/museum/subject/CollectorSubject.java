package ru.cristalix.museum.museum.subject;

import clepto.bukkit.Lemonade;
import lombok.Setter;
import lombok.experimental.Delegate;
import lombok.val;
import net.minecraft.server.v1_12_R1.EntityArmorStand;
import net.minecraft.server.v1_12_R1.EnumItemSlot;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;
import ru.cristalix.museum.App;
import ru.cristalix.museum.data.subject.SubjectInfo;
import ru.cristalix.museum.museum.Museum;
import ru.cristalix.museum.museum.collector.CollectorNavigator;
import ru.cristalix.museum.museum.map.CollectorSubjectPrototype;
import ru.cristalix.museum.museum.map.SubjectPrototype;
import ru.cristalix.museum.museum.map.SubjectType;
import ru.cristalix.museum.museum.subject.skeleton.Piece;
import ru.cristalix.museum.player.User;

public class CollectorSubject implements Subject {

	private final CollectorSubjectPrototype prototype;

	@Delegate
	private final SubjectInfo info;

	@Setter
	private CollectorNavigator navigator;
	private final Piece piece;

	public CollectorSubject(Museum museum, SubjectInfo info, SubjectPrototype prototype) {
		this.info = info;
		this.prototype = (CollectorSubjectPrototype) prototype;
		EntityArmorStand armorStand = new EntityArmorStand(App.getApp().getNMSWorld());
		Lemonade lemonade = Lemonade.get(this.prototype.getAddress());
		ItemStack item = lemonade == null ? new ItemStack(Material.WORKBENCH) : lemonade.render();
		armorStand.setSlot(EnumItemSlot.HEAD, CraftItemStack.asNMSCopy(item));
		armorStand.setBasePlate(false);
		armorStand.setCustomName(prototype.getTitle());
		armorStand.setCustomNameVisible(true);
		armorStand.setInvisible(true);
		armorStand.setNoGravity(true);
		this.navigator = null;
		this.piece = new Piece(armorStand, null);
	}

	@Override
	public SubjectInfo generateInfo() {
		return info;
	}

	@Override
	public SubjectType<?> getType() {
		return SubjectType.COLLECTOR;
	}

	@Override
	public void show(User user) {
		val location = getLocation(System.currentTimeMillis());
		piece.show(user.getPlayer(), location);
	}

	public void move(User user, long iteration) {
		if (navigator == null)
			return;
		val location = getLocation(iteration);
		user.getCoins().removeIf(coin -> coin.pickUp(user, location, prototype.getRadius()));
		piece.update(user.getPlayer(), location);
	}

	@Override
	public void hide(User user) {
		piece.hide(user.getPlayer());
	}

	private Location getLocation(long time) {
		int secondsPerLap = (int) prototype.getSpeed();
		return navigator.getLocation(time % secondsPerLap / (double) secondsPerLap);
	}
}
