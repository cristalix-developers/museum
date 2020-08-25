package museum.museum.subject;

import clepto.cristalix.mapservice.Box;
import lombok.Getter;
import org.bukkit.Location;
import ru.cristalix.core.math.V3;
import ru.cristalix.core.util.UtilV3;
import museum.data.SubjectInfo;
import museum.museum.map.SubjectPrototype;
import museum.museum.subject.skeleton.Skeleton;
import museum.museum.subject.skeleton.SkeletonPrototype;
import museum.player.User;
import museum.prototype.Managers;

/**
 * @author func 22.05.2020
 * @project Museum
 */
@Getter
public class SkeletonSubject extends Subject {

	private Skeleton skeleton;
	private float skeletonYaw;
	private Location skeletonLocation;

	public SkeletonSubject(SubjectPrototype prototype, SubjectInfo info, User owner) {
		super(prototype, info, owner);

		V3 origin = prototype.getRelativeOrigin();
		this.skeletonLocation = prototype.getBox().getMin().clone().add(origin.getX(), origin.getY(), origin.getZ());

		if (info.metadata == null) return;

		String[] ss = info.metadata.split(":");
		String skeletonAddress = ss[0];
		SkeletonPrototype skeletonProto = Managers.skeleton.getPrototype(skeletonAddress);
		if (skeletonProto == null) return;
		this.skeleton = owner.getSkeletons().get(skeletonProto);
		this.skeletonYaw = Float.parseFloat(ss[1]);
	}

	@Override
	public Allocation allocate(Location origin) {
		if (origin == null) {
			skeletonLocation = null;
			return super.allocate(null);
		}
		Box box = prototype.getBox();
		V3 o = prototype.getRelativeOrigin();
		this.skeletonLocation = box.transpose(
				UtilV3.fromVector(box.getMin().toVector()),
				cachedInfo.getRotation(),
				new V3(0, 0, 0),
				(int) o.getX(),
				(int) o.getY(),
				(int) o.getZ()
		);

		this.skeletonLocation.setYaw(skeletonYaw);

		return super.allocate(origin);
	}

	@Override
	public void updateInfo() {
		if (skeleton == null) cachedInfo.metadata = null;
		else cachedInfo.metadata = skeleton.getPrototype().getAddress() + ":" + skeletonYaw;
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

	public double getIncome() {
		if (skeleton == null) return 0;
		return skeleton.getUnlockedFragments().size() * skeleton.getPrototype().getRarity().getIncrease();
	}

}
