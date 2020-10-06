package museum.museum.subject;

import clepto.bukkit.B;
import com.google.common.collect.Maps;
import com.google.gson.reflect.TypeToken;
import lombok.val;
import museum.App;
import museum.data.SubjectInfo;
import museum.museum.map.StallPrototype;
import museum.museum.map.SubjectPrototype;
import museum.museum.subject.product.FoodProduct;
import museum.player.User;
import museum.worker.WorkerUtil;
import ru.cristalix.core.GlobalSerializers;

import java.util.Map;

/**
 * @author func 05.10.2020
 * @project museum
 */
public class StallSubject extends Subject {

	private final Map<FoodProduct, Integer> food;

	public StallSubject(SubjectPrototype prototype, SubjectInfo info, User owner) {
		super(prototype, info, owner);
		food = info.metadata == null ?
				Maps.newHashMap() :
				GlobalSerializers.fromJson(info.metadata, new TypeToken<Map<String, Integer>>() {}.getType());
	}

	@Override
	protected void updateInfo() {
		super.updateInfo();
		if (cachedInfo == null || !isAllocated())
			return;
		cachedInfo.metadata = food == null || food.isEmpty() ? "" : GlobalSerializers.toJson(food);
	}

	@Override
	public void setAllocation(Allocation allocation) {
		super.setAllocation(allocation);
		if (allocation == null)
			return;
		//WorkerUtil.reload(owner);
	}
}