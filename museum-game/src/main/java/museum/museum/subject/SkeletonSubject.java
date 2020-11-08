package museum.museum.subject;

import clepto.bukkit.world.Box;
import clepto.bukkit.world.Orientation;
import clepto.math.V3;
import lombok.Getter;
import museum.data.SubjectInfo;
import museum.museum.Museum;
import museum.museum.map.SubjectPrototype;
import museum.museum.subject.skeleton.Skeleton;
import museum.museum.subject.skeleton.SkeletonPrototype;
import museum.display.V5;
import museum.player.User;
import museum.prototype.Managers;

import static museum.display.Piece.orientedOffset;

/**
 * @author func 22.05.2020
 * @project Museum
 */
@Getter
public class SkeletonSubject extends Subject {

	private Skeleton skeleton;
	private V5 skeletonLocation;

	public SkeletonSubject(SubjectPrototype prototype, SubjectInfo info, User owner) {
		super(prototype, info, owner);

		V3 origin = prototype.getRelativeOrigin();
		this.skeletonLocation = V5.fromLocation(prototype.getBox().getMin()).add(origin.getX(), origin.getY(), origin.getZ());

		if (info.metadata == null) return;

		String[] ss = info.metadata.split(":");
		String skeletonAddress = ss[0];
		SkeletonPrototype skeletonProto = Managers.skeleton.getPrototype(skeletonAddress);
		if (skeletonProto == null) return;
		this.skeleton = owner.getSkeletons().supply(skeletonProto);
		this.skeletonLocation.yaw = Float.parseFloat(ss[1]);
	}

	@Override
	public void setAllocation(Allocation allocation) {
		super.setAllocation(allocation);
		if (allocation == null) skeletonLocation = null;
		else {
			float rot = this.skeletonLocation == null ? 0 : this.skeletonLocation.yaw;
			Box box = prototype.getBox();
			V3 o = prototype.getRelativeOrigin();
			this.skeletonLocation = V5.fromLocation(box.transpose(
					V3.of(box.getMin().getX(), box.getMin().getY(), box.getMin().getZ()),
					Orientation.values()[cachedInfo.getRotation().ordinal()],
					V3.of(o.getX(), 0, o.getZ()),
					(int) o.getX(),
					(int) o.getY(),
					(int) o.getZ()
																 ));
			this.skeletonLocation.setYaw(rot);

			this.updateSkeleton(false);
		}
	}

	public void updateSkeleton(boolean sendUpdates) {
		Allocation allocation = this.getAllocation();
		this.updateInfo();
		if (allocation == null || this.skeleton == null) return;
		V5 absoluteLocation = V5.fromLocation(allocation.getOrigin()).add(this.skeletonLocation);
		skeleton.getUnlockedFragments().forEach(fragment ->
				allocation.allocatePiece(fragment, orientedOffset(absoluteLocation, skeleton.getPrototype().getOffset(fragment)), sendUpdates));
		if (owner.getState() == null)
			return;
		((Museum) owner.getState()).updateIncrease();
	}

	@Override
	public void updateInfo() {
		super.updateInfo();
		if (skeleton == null) cachedInfo.metadata = null;
		else
			cachedInfo.metadata = skeleton.getPrototype().getAddress() + ":" + (skeletonLocation == null ? 0 : skeletonLocation.yaw);
	}

	@Override
	public double getIncome() {
		if (skeleton == null)
			return 0;
		return skeleton.getUnlockedFragments().size() * (double) skeleton.getPrototype().getPrice() / 300;
	}

	public void setSkeleton(Skeleton skeleton) {
		this.skeleton = skeleton;
		updateInfo();
	}

}
