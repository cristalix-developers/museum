package ru.cristalix.museum.museum.subject;

import clepto.bukkit.Lemonade;
import lombok.Getter;
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

public class CollectorSubject extends SimpleSubject {

	private final CollectorSubjectPrototype prototype;
	@Delegate
	private final SubjectInfo info;
	@Getter
	private final int id;
	private final Piece piece;
	@Setter
	private CollectorNavigator navigator;

	public CollectorSubject(Museum museum, SubjectInfo info, SubjectPrototype prototype) {
		super(museum, info, prototype);
		this.info = info;
		this.prototype = (CollectorSubjectPrototype) prototype;
		EntityArmorStand armorStand = new EntityArmorStand(App.getApp().getNMSWorld());
		Lemonade lemonade = Lemonade.get(this.prototype.getAddress());
		ItemStack item = lemonade == null ? new ItemStack(Material.WORKBENCH) : lemonade.render();
		armorStand.setSlot(EnumItemSlot.HEAD, CraftItemStack.asNMSCopy(item));
		armorStand.setCustomName(prototype.getTitle());
		armorStand.setCustomNameVisible(true);
		this.navigator = null;
		this.piece = new Piece(armorStand, null);
		this.id = info.getMetadata() == null ? 0 : Integer.parseInt(info.getMetadata());
	}

	@Override
	public SubjectInfo generateInfo() {
		info.metadata = String.valueOf(id);
		return info;
	}

	@Override
	public SubjectType<?> getType() {
		return SubjectType.COLLECTOR;
	}

	@Override
	public void show(User user) {
		super.show(user);
		piece.show(user.getPlayer(), getLocation(System.currentTimeMillis()), false);
	}

	public void move(User user, long iteration) {
		if (navigator == null)
			return;
		val location = getLocation(iteration);
		user.getCoins().removeIf(coin -> coin.pickUp(user, location, prototype.getRadius(), id));
		piece.update(user.getPlayer(), location);
	}

	@Override
	public void hide(User user) {
		super.hide(user);
		piece.hide(user.getPlayer());
	}

	private Location getLocation(long time) {
		int secondsPerLap = 20000 / (int) prototype.getSpeed();
		return navigator.getLocation(time % secondsPerLap / (double) secondsPerLap);
	}
}
