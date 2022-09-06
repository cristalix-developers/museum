package museum.museum.subject;

import lombok.Getter;
import museum.data.SubjectInfo;
import museum.museum.map.SubjectPrototype;
import museum.player.User;
import museum.prototype.Storable;
import ru.cristalix.core.math.D2;
import ru.cristalix.core.util.UtilV3;

import java.util.ArrayList;
import java.util.UUID;

/**
 * @author func 22.05.2020
 * @project Museum
 */
@Getter
public class Subject extends Storable<SubjectInfo, SubjectPrototype> {

	private Allocation allocation;
	private final ArrayList<UUID> bannerUUIDs = new ArrayList<>();
	private SubjectPrototype.SubjectDataForClient dataForClient;

	public Subject(SubjectPrototype prototype, SubjectInfo info, User owner) {
		super(prototype, info, owner);
	}

	public boolean isAllocated() {
		return allocation != null;
	}

	public void setAllocation(Allocation allocation) {
		this.allocation = allocation;
		this.updateInfo();
		if (allocation != null) {
			dataForClient = new SubjectPrototype.SubjectDataForClient(
					prototype.getAddress(),
					prototype.getTitle(),
					allocation.getMin(),
					allocation.getMax(),
					prototype.getPrice(),
					cachedInfo.uuid
			);
		}
	}

	@Override
	protected void updateInfo() {
		this.cachedInfo.location = isAllocated() ? UtilV3.fromVector(allocation.getOrigin().toVector()) : null;
		this.cachedInfo.rotation = isAllocated() ? D2.PX : null;
	}

	public double getIncome() {
		return 0;
	}

	public void acceptClick() {}
}