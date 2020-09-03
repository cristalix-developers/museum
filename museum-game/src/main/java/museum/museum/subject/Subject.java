package museum.museum.subject;

import clepto.bukkit.B;
import lombok.Getter;
import org.bukkit.Location;
import ru.cristalix.core.util.UtilV3;
import museum.App;
import museum.prototype.Storable;
import museum.data.SubjectInfo;
import museum.museum.map.SubjectPrototype;
import museum.player.User;

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

	public void hide(User user, boolean playEffects) {
		if (allocation == null) return;
		allocation.getHidePackets().forEach(user::sendPacket);
		if (playEffects) allocation.getDestroyPackets().forEach(user::sendPacket);
		allocate(null);
	}

	public double getIncome() {
		return 0;
	}

}
