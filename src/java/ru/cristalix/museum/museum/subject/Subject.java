package ru.cristalix.museum.museum.subject;

import clepto.bukkit.B;
import lombok.Getter;
import org.bukkit.Location;
import ru.cristalix.core.util.UtilV3;
import ru.cristalix.museum.App;
import ru.cristalix.museum.prototype.Storable;
import ru.cristalix.museum.data.SubjectInfo;
import ru.cristalix.museum.museum.map.SubjectPrototype;
import ru.cristalix.museum.player.User;

/**
 * @author func 22.05.2020
 * @project Museum
 */
public class Subject extends Storable<SubjectInfo, SubjectPrototype> {

	@Getter
	private Allocation allocation;

	public Subject(SubjectPrototype prototype, SubjectInfo info, User owner) {
		super(prototype, info, owner);
		B.run(() -> allocate(UtilV3.toLocation(info.getLocation(), App.getApp().getWorld())));
	}

	public Allocation allocate(Location origin) {
		if (origin == null) System.out.println("Clearing allocation for " + prototype.getAddress());
		return this.allocation = Allocation.allocate(cachedInfo, prototype, origin);
	}

	public boolean isAllocated() {
		return allocation != null;
	}

	public void show(User user) {
		if (allocation != null) allocation.getShowPackets().forEach(user::sendPacket);
	}

	public void hide(User user, boolean visually) {
		if (allocation == null) return;
		allocation.getHidePackets().forEach(user::sendPacket);
		if (visually) allocation.getDestroyPackets().forEach(user::sendPacket);
		allocate(null);
	}

	public double getIncome() {
		return 0;
	}

	@Override
	protected void updateInfo() {
		// todo: useless method
	}
}
