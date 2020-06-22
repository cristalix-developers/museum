package ru.cristalix.museum.museum.subject;

import lombok.Getter;
import org.bukkit.Location;
import ru.cristalix.core.math.V3;
import ru.cristalix.museum.data.subject.SubjectInfo;
import ru.cristalix.museum.museum.Museum;
import ru.cristalix.museum.museum.map.SubjectPrototype;
import ru.cristalix.museum.museum.subject.skeleton.Skeleton;
import ru.cristalix.museum.player.User;

/**
 * @author func 22.05.2020
 * @project Museum
 */
@Getter
public class SkeletonSubject extends SimpleSubject {

	private final Skeleton skeleton;
	private final Location skeletonLocation;

	public SkeletonSubject(Museum museum, SubjectInfo info, SubjectPrototype prototype) {
		super(museum, info, prototype);

		V3 o = prototype.getRelativeOrigin();
		this.skeletonLocation = prototype.getBox().getMin().clone().add(o.getX(), o.getY(), o.getZ());

		if (info.metadata == null) {
			this.skeleton = null;
			return;
		}

		String[] ss = info.metadata.split(":");
		String skeletonAddress = ss[0];
		this.skeleton = museum.getOwner().getSkeleton(skeletonAddress);
		this.skeletonLocation.setYaw(Float.parseFloat(ss[1]));
	}

	@Override
	public SubjectInfo generateInfo() {
		info.metadata = skeleton.getAddress() + ":" + location.getYaw();
		return super.generateInfo();
	}

	@Override
	public void show(User user) {
		super.show(user);
		if (skeleton == null)
			return;

		skeleton.getUnlockedFragments().forEach(fragment -> fragment.show(user.getPlayer(), skeletonLocation));
	}

	@Override
	public void hide(User user) {
		super.hide(user);
		if (skeleton == null)
			return;
		skeleton.getUnlockedFragments().forEach(fragment -> fragment.hide(user.getPlayer()));
	}

	@Override
	public double getIncome() {
		// ToDo: Этот код будет писать фанк
		return 0;
	}

}
