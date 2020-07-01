package ru.cristalix.museum.museum.subject;

import clepto.bukkit.B;
import lombok.Getter;
import org.bukkit.Location;
import ru.cristalix.core.util.UtilV3;
import ru.cristalix.museum.App;
import ru.cristalix.museum.data.subject.SubjectInfo;
import ru.cristalix.museum.museum.map.SubjectPrototype;
import ru.cristalix.museum.museum.map.SubjectType;
import ru.cristalix.museum.player.User;

/**
 * @author func 22.05.2020
 * @project Museum
 */
public class SimpleSubject implements Subject {

	protected final User owner;
	protected final SubjectInfo info;
	protected final SubjectPrototype prototype;

	@Getter
	private Allocation allocation;

	public SimpleSubject(User owner, SubjectInfo info, SubjectPrototype prototype) {
		this.owner = owner;
		this.info = info;
		this.prototype = prototype;
		B.run(() -> allocate(UtilV3.toLocation(info.getLocation(), App.getApp().getWorld())));
	}

	@Override
	public SubjectType<?> getType() {
		return SubjectType.DECORATION;
	}

	public Allocation allocate(Location origin) {
		if (origin == null) System.out.println("Clearing allocation for " + prototype.getAddress());
		return this.allocation = Allocation.allocate(info, prototype, origin);
	}

	@Override
	public void show(User user) {
		if (allocation != null) allocation.getShowPackets().forEach(user::sendPacket);
	}

	@Override
	public void hide(User user, boolean visually) {
		if (allocation == null) return;
		allocation.getHidePackets().forEach(user::sendPacket);
		if (visually) allocation.getDestroyPackets().forEach(user::sendPacket);
	}

	@Override
	public SubjectInfo generateInfo() {
		return info;
	}

}
