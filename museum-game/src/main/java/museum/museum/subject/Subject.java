package museum.museum.subject;

import lombok.Getter;
import museum.data.SubjectInfo;
import museum.museum.map.SubjectPrototype;
import museum.player.User;
import museum.prototype.Storable;
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
	}

	public boolean isAllocated() {
		return allocation != null;
	}

	public void setAllocation(Allocation allocation) {
		this.allocation = allocation;
		this.updateInfo();
	}

	@Override
	protected void updateInfo() {
		super.updateInfo();
		this.cachedInfo.location = isAllocated() ? UtilV3.fromVector(allocation.getOrigin().toVector()) : null;
	}

	public double getIncome() {
		return 0;
	}

}
