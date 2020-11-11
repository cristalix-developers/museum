package museum.museum.subject;

import lombok.Getter;
import museum.data.SubjectInfo;
import museum.misc.Relic;
import museum.museum.Museum;
import museum.museum.map.SubjectPrototype;
import museum.player.User;

/**
 * @author func 11.11.2020
 * @project museum
 */
@Getter
public class RelicShowcaseSubject extends Subject {

	private Relic relic;

	public RelicShowcaseSubject(SubjectPrototype prototype, SubjectInfo info, User owner) {
		super(prototype, info, owner);
		this.relic = info.metadata != null && !info.metadata.isEmpty() ? new Relic(info.metadata) : null;
	}

	@Override
	public void setAllocation(Allocation allocation) {
		super.setAllocation(allocation);
		if (allocation == null)
			relic = null;
		else {
			this.updateRelic();
		}
	}

	public void updateRelic() {
		Allocation allocation = this.getAllocation();
		this.updateInfo();
		if (allocation == null || this.relic == null)
			return;
		/*V4 absoluteLocation = V4.fromLocation(allocation.getOrigin()).add(this.re);
		skeleton.getUnlockedFragments().forEach(fragment ->
				allocation.allocatePiece(fragment, orientedOffset(absoluteLocation, skeleton.getPrototype().getOffset(fragment)), sendUpdates));*/
		if (owner.getState() == null)
			return;
		((Museum) owner.getState()).updateIncrease();
	}

	@Override
	public void updateInfo() {
		super.updateInfo();
		cachedInfo.metadata = relic == null ? null : relic.getPrototypeAddress();
	}

	@Override
	public double getIncome() {
		if (relic == null)
			return 0;
		return relic.getPrice();
	}

	public void setRelic(Relic relic) {
		this.relic = relic;
		updateInfo();
	}
}
