package museum.museum.subject;

import clepto.cristalix.mapservice.Box;
import lombok.Getter;
import museum.data.SubjectInfo;
import museum.museum.map.SubjectPrototype;
import museum.museum.subject.skeleton.Skeleton;
import museum.museum.subject.skeleton.SkeletonPrototype;
import museum.museum.subject.skeleton.V4;
import museum.player.User;
import museum.prototype.Managers;
import org.bukkit.Location;
import ru.cristalix.core.math.V3;
import ru.cristalix.core.util.UtilV3;

import static museum.museum.subject.skeleton.Displayable.orientedOffset;

/**
 * @author func 22.05.2020
 * @project Museum
 */
@Getter
public class SkeletonSubject extends Subject {

	private Skeleton skeleton;
	private V4 skeletonLocation;

	public SkeletonSubject(SubjectPrototype prototype, SubjectInfo info, User owner) {
		super(prototype, info, owner);

		V3 origin = prototype.getRelativeOrigin();
		this.skeletonLocation = V4.fromLocation(prototype.getBox().getMin()).add(origin.getX(), origin.getY(), origin.getZ());

		if (info.metadata == null) return;

		String[] ss = info.metadata.split(":");
		String skeletonAddress = ss[0];
		SkeletonPrototype skeletonProto = Managers.skeleton.getPrototype(skeletonAddress);
		if (skeletonProto == null) return;
		this.skeleton = owner.getSkeletons().supply(skeletonProto);
		this.skeletonLocation.rot = Float.parseFloat(ss[1]);
	}

	@Override
	public Allocation allocate(Location origin) {
		Allocation allocation = super.allocate(origin);
		if (origin == null) skeletonLocation = null;
		else {
			float rot = this.skeletonLocation == null ? 0 : this.skeletonLocation.rot;
			Box box = prototype.getBox();
			V3 o = prototype.getRelativeOrigin();
			this.skeletonLocation = V4.fromLocation(box.transpose(
					UtilV3.fromVector(box.getMin().toVector()),
					cachedInfo.getRotation(),
					new V3(o.getX(), 0, o.getZ()),
					(int) o.getX(),
					(int) o.getY(),
					(int) o.getZ()
			));
			this.skeletonLocation.setRot(rot);

		}

		return allocation;
	}

	@Override
	public void updateInfo() {
		super.updateInfo();
		if (skeleton == null) cachedInfo.metadata = null;
		else cachedInfo.metadata = skeleton.getPrototype().getAddress() + ":" + (skeletonLocation == null ? 0 : skeletonLocation.rot);
	}

	@Override
	public void show(User user) {
		super.show(user);
		if (skeleton == null) return;
		if (!isAllocated()) return;
		V4 absoluteLocation = V4.fromLocation(this.getAllocation().getOrigin()).add(this.skeletonLocation);
		skeleton.getUnlockedFragments().forEach(fragment ->
				fragment.show(user, orientedOffset(absoluteLocation, skeleton.getPrototype().getOffset(fragment))));
	}

	@Override
	public void hide(User user) {
		super.hide(user);
		if (skeleton != null)
			skeleton.getPrototype().hide(user);
	}

	public double getIncome() {
		if (skeleton == null)
			return 0;
		return skeleton.getUnlockedFragments().size() * skeleton.getPrototype().getPrice();
	}

	public void setSkeleton(Skeleton skeleton) {
		this.skeleton = skeleton;
		updateInfo();
	}

}
