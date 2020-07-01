package ru.cristalix.museum.museum.subject;

import clepto.cristalix.Box;
import lombok.Getter;
import org.bukkit.Location;
import ru.cristalix.core.math.V3;
import ru.cristalix.core.util.UtilV3;
import ru.cristalix.museum.data.subject.SubjectInfo;
import ru.cristalix.museum.museum.map.SubjectPrototype;
import ru.cristalix.museum.museum.map.SubjectType;
import ru.cristalix.museum.museum.subject.skeleton.Skeleton;
import ru.cristalix.museum.player.User;

/**
 * @author func 22.05.2020
 * @project Museum
 */
@Getter
public class SkeletonSubject extends SimpleSubject {

	private final Skeleton skeleton;
	private float skeletonYaw;
	private Location skeletonLocation;

	public SkeletonSubject(User owner, SubjectInfo info, SubjectPrototype prototype) {
		super(owner, info, prototype);

		V3 origin = prototype.getRelativeOrigin();
		this.skeletonLocation = prototype.getBox().getMin().clone().add(origin.getX(), origin.getY(), origin.getZ());

		if (info.metadata == null) {
			this.skeleton = null;
			return;
		}

		String[] ss = info.metadata.split(":");
		String skeletonAddress = ss[0];
		this.skeleton = owner.getSkeleton(skeletonAddress);
		this.skeletonYaw = Float.parseFloat(ss[1]);
	}

	@Override
	public Allocation allocate(Location origin) {
		Box box = prototype.getBox();
		V3 o = prototype.getRelativeOrigin();
		// ToDo: Debug, this should count from boxMin, not from origin (center)
		this.skeletonLocation = box.transpose(
				UtilV3.fromVector(origin.toVector()),
				info.getRotation(),
				new V3(0, 0, 0),
				(int) o.getX(),
				(int) o.getY(),
				(int) o.getZ()
										  );

		this.skeletonLocation.setYaw(skeletonYaw);

		return super.allocate(origin);
	}

	@Override
	public SubjectInfo generateInfo() {
		if (skeleton == null) info.metadata = null;
		else info.metadata = skeleton.getAddress() + ":" + skeletonYaw;
		return super.generateInfo();
	}

	@Override
	public void show(User user) {
		super.show(user);
		if (skeleton == null) return;
		skeleton.getUnlockedFragments().forEach(fragment -> fragment.show(user.getPlayer(), skeletonLocation, false));
	}

	@Override
	public void hide(User user, boolean visually) {
		super.hide(user, visually);
		if (skeleton == null) return;
		skeleton.getUnlockedFragments().forEach(fragment -> fragment.hide(user.getPlayer()));
	}

	@Override
	public double getIncome() {
		if (skeleton == null) return 0;
		return skeleton.getUnlockedFragments().size() * skeleton.getSkeletonPrototype().getRarity().getIncrease();
	}

	@Override
	public SubjectType<?> getType() {
		return SubjectType.SKELETON_CASE;
	}

}
