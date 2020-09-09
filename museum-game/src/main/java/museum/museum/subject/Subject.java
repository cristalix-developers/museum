package museum.museum.subject;

import clepto.bukkit.B;
import lombok.Getter;
import museum.App;
import museum.data.SubjectInfo;
import museum.museum.map.SubjectPrototype;
import museum.player.User;
import museum.prototype.Storable;
import org.bukkit.Location;
import ru.cristalix.core.util.UtilV3;

/**
 * @author func 22.05.2020
 * @project Museum
 */
@Getter
public class Subject extends Storable<SubjectInfo, SubjectPrototype> {

	private Allocation allocation;

	public Subject(SubjectPrototype prototype, SubjectInfo info, User owner) {
		super(prototype, info, owner);
		B.run(() -> allocate(info.getLocation() == null ? null : UtilV3.toLocation(info.getLocation(), App.getApp().getWorld())));
	}

	public Allocation allocate(Location origin) {
		if (origin == null)
			System.out.println("Clearing allocation for " + prototype.getAddress());
		return this.allocation = Allocation.allocate(cachedInfo, prototype, origin);
	}

	public boolean isAllocated() {
		return allocation != null;
	}

	@Override
	protected void updateInfo() {
		super.updateInfo();
		this.cachedInfo.location = allocation == null ? null : UtilV3.fromVector(allocation.getOrigin().toVector());
	}

	public void show(User user) {
		if (allocation != null) allocation.getShowPackets().forEach(user::sendPacket);
	}

	public void hide(User user) {
		if (allocation == null)
			return;
		allocation.getHidePackets().forEach(user::sendPacket);
	}

	public double getIncome() {
		return 0;
	}

}
