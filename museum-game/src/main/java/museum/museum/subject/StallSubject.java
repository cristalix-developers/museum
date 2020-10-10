package museum.museum.subject;

import com.google.common.collect.Maps;
import com.google.gson.reflect.TypeToken;
import lombok.val;
import museum.data.SubjectInfo;
import museum.museum.map.StallPrototype;
import museum.museum.map.SubjectPrototype;
import museum.museum.subject.product.FoodProduct;
import museum.museum.subject.skeleton.V4;
import museum.player.User;
import museum.worker.NpcWorker;
import ru.cristalix.core.GlobalSerializers;
import ru.cristalix.core.util.UtilV3;

import java.util.Map;

/**
 * @author func 05.10.2020
 * @project museum
 */
public class StallSubject extends Subject {

	private final Map<FoodProduct, Integer> food;
	private final NpcWorker worker;

	public StallSubject(SubjectPrototype prototype, SubjectInfo info, User owner) {
		super(prototype, info, owner);
		food = info.metadata == null ?
				Maps.newHashMap() :
				GlobalSerializers.fromJson(info.metadata, new TypeToken<Map<String, Integer>>() {
				}.getType());
		worker = ((StallPrototype) prototype).getWorker().get();
	}

	@Override
	protected void updateInfo() {
		if (cachedInfo == null || !isAllocated())
			return;
		cachedInfo.metadata = food == null || food.isEmpty() ? "" : GlobalSerializers.toJson(food);
	}

	@Override
	public void setAllocation(Allocation allocation) {
		super.setAllocation(allocation);
		if (cachedInfo != null && allocation != null) {
			// todo: what the fuck this hardcode nums
			val spawn = ((StallPrototype) prototype).getSpawn().clone()
					.subtract(prototype.getBox().getCenter().clone().subtract(0, 4, 0))
					.add(UtilV3.toVector(cachedInfo.location));
			worker.setLocation(spawn);
			allocation.allocateDisplayable(worker);
		}
	}

	public void update() {
		worker.update(owner, V4.fromLocation(owner.getLocation()));
	}
}