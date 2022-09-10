package museum.museum.subject;

import com.google.common.collect.Maps;
import com.google.gson.reflect.TypeToken;
import lombok.Getter;
import lombok.val;
import museum.data.SubjectInfo;
import museum.multi_chat.ChatType;
import museum.multi_chat.MultiChatUtil;
import museum.museum.map.SubjectPrototype;
import museum.museum.subject.product.FoodProduct;
import museum.player.User;
import museum.util.MessageUtil;
import ru.cristalix.core.GlobalSerializers;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

/**
 * @author func 05.10.2020
 * @project museum
 */
public class StallSubject extends Subject implements Incomeble {
	@Getter
	private final Map<FoodProduct, Integer> food;

	public StallSubject(SubjectPrototype prototype, SubjectInfo info, User owner) {
		super(prototype, info, owner);
		if (info.metadata == null)
			food = Maps.newHashMap();
		else {
			Map<FoodProduct, Integer> savedFood = GlobalSerializers.fromJson(info.metadata, new TypeToken<Map<FoodProduct, Integer>>() {
			}.getType());
			if (savedFood == null) {
				food = Maps.newHashMap();
			} else {
				food = savedFood;
			}
		}
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
	}

	@Override
	public void handle(double... args) {
		if (!isAllocated())
			return;
		if (args[0] % (140 * 20L) != 0)
			return;
		Set<FoodProduct> potentialFood = food.keySet();
		if (potentialFood.isEmpty()) {
			MultiChatUtil.sendMessage(owner.getPlayer(), ChatType.SYSTEM, MessageUtil.get("no-product"));
			return;
		}
		int randomFoodIndex = (int) (Math.random() * potentialFood.size());
		Map.Entry<FoodProduct, Integer> choiceFood = new ArrayList<>(food.entrySet()).get(randomFoodIndex);
		val key = choiceFood.getKey();
		food.replace(key, choiceFood.getValue() - 1);
		if (food.get(key) <= 0)
			food.remove(key);
		MessageUtil.find("sell-product")
				.set("title", key.getName())
				.set("cost", key.getCost())
				.send(owner);
		owner.depositMoneyWithBooster(key.getCost());
	}
}