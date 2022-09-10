package museum.museum.subject;

import clepto.bukkit.world.Box;
import clepto.bukkit.world.Orientation;
import implario.math.V3;
import lombok.Getter;
import lombok.Setter;
import museum.data.SubjectInfo;
import museum.museum.Museum;
import museum.museum.map.SubjectPrototype;
import museum.museum.subject.skeleton.Skeleton;
import museum.museum.subject.skeleton.SkeletonPrototype;
import museum.museum.subject.skeleton.V4;
import museum.player.User;
import museum.prototype.Managers;
import museum.util.BannerUtil;

import static museum.museum.subject.skeleton.Piece.orientedOffset;

/**
 * @author func 22.05.2020
 * @project Museum
 */
@Getter
public class SkeletonSubject extends Subject {

    private Skeleton skeleton;
    private V4 skeletonLocation;
    @Setter
    private int level = 1;

    public SkeletonSubject(SubjectPrototype prototype, SubjectInfo info, User owner) {
        super(prototype, info, owner);

        V3 origin = prototype.getRelativeOrigin();
        this.skeletonLocation = V4.fromLocation(prototype.getBox().getMin()).add(origin.getX(), origin.getY(), origin.getZ());

        if (info.metadata == null) return;
        String[] ss = info.metadata.split(":");
        try {
            this.level = Integer.parseInt(ss[2]);
            this.level = this.level >= 50 ? 50 : this.level;
        } catch (Exception exception) {
            this.level = 1;
        }
        String skeletonAddress = ss[0];
        SkeletonPrototype skeletonProto = Managers.skeleton.getPrototype(skeletonAddress);
        if (skeletonProto == null) return;
        this.skeleton = owner.getSkeletons().supply(skeletonProto);
        this.skeletonLocation.rot = Float.parseFloat(ss[1]);
    }

    @Override
    public void setAllocation(Allocation allocation) {
        super.setAllocation(allocation);
        if (allocation == null) skeletonLocation = null;
        else {
            float rot = this.skeletonLocation == null ? 0 : this.skeletonLocation.rot;
            Box box = prototype.getBox();
            V3 o = prototype.getRelativeOrigin();
            this.skeletonLocation = V4.fromLocation(box.transpose(
                    V3.of(box.getMin().getX(), box.getMin().getY(), box.getMin().getZ()),
                    Orientation.values()[cachedInfo.getRotation().ordinal()],
                    V3.of(o.getX(), 0, o.getZ()),
                    (int) o.getX(),
                    (int) o.getY(),
                    (int) o.getZ()
            ));
            this.skeletonLocation.setRot(rot);

            this.updateSkeleton(false);
        }
    }

    public void updateSkeleton(boolean sendUpdates) {
        Allocation allocation = this.getAllocation();
        this.updateInfo();
        if (allocation == null || this.skeleton == null) return;
        V4 absoluteLocation = V4.fromLocation(allocation.getOrigin()).add(this.skeletonLocation);
        skeleton.getUnlockedFragments().forEach(fragment ->
                allocation.allocatePiece(fragment, orientedOffset(absoluteLocation, skeleton.getPrototype().getOffset(fragment)), sendUpdates));
        if (owner.getState() == null)
            return;
        if (owner.getState() instanceof Museum)
            ((Museum) owner.getState()).updateIncrease();
        BannerUtil.updateBanners(this);
        owner.updateIncome();
    }

    @Override
    public void updateInfo() {
        super.updateInfo();
        if (skeleton == null) cachedInfo.metadata = "::" + level;
        else
            cachedInfo.metadata = skeleton.getPrototype().getAddress() + ":" + (skeletonLocation == null ? 0 : skeletonLocation.rot) + ":" + level;
    }

    @Override
    public double getIncome() {
        if (skeleton == null)
            return 0;
        return skeleton.getUnlockedFragments().size() * (double) skeleton.getPrototype().getPrice() / 20 * (level / 50F + 1);
    }

    public void setSkeleton(Skeleton skeleton) {
        this.skeleton = skeleton;
        updateInfo();
        BannerUtil.updateBanners(this);
        this.getOwner().updateIncome();
    }

}
