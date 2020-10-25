package museum.museum.subject;

import com.google.common.collect.Maps;
import com.google.gson.reflect.TypeToken;
import lombok.Getter;
import lombok.val;
import museum.data.SubjectInfo;
import museum.museum.map.StallPrototype;
import museum.museum.map.SubjectPrototype;
import museum.museum.subject.product.FoodProduct;
import museum.museum.subject.skeleton.V4;
import museum.player.User;
import museum.util.MessageUtil;
import museum.worker.NpcWorker;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import ru.cristalix.core.GlobalSerializers;
import ru.cristalix.core.util.UtilV3;

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
	private final NpcWorker worker;

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
		worker = ((StallPrototype) prototype).getWorker().get();
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
		if (cachedInfo != null && allocation != null) {
			// todo: what the fuck this hardcode nums
			val spawn = ((StallPrototype) prototype).getSpawn().clone()
					.subtract(prototype.getBox().getCenter().clone().subtract(0, 4, 0))
					.add(UtilV3.toVector(cachedInfo.location));
			worker.setLocation(spawn);
			allocation.allocateDisplayable(worker);
		}
	}

	@Override
	public void handle(double... args) {
		if (!isAllocated())
			return;
		if (args[0] % (120 * 20L) != 0)
			return;
		Set<FoodProduct> potentialFood = food.keySet();
		if (potentialFood.isEmpty()) {
			TextComponent message = new TextComponent(MessageUtil.get("no-product"));
			message.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/go"));
			owner.getPlayer().sendMessage(message);
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
		owner.setMoney(owner.getMoney() + key.getCost());
	}

	public void rotateCustomerHead() {
		worker.update(owner, V4.fromLocation(owner.getLocation()));
	}
}