package museum.museum.subject;

import com.google.common.collect.Maps;
import com.google.gson.reflect.TypeToken;
import lombok.Getter;
import lombok.val;
import me.func.mod.Npc;
import me.func.protocol.npc.NpcBehaviour;
import me.func.protocol.npc.NpcData;
import museum.data.SubjectInfo;
import museum.museum.map.StallPrototype;
import museum.museum.map.SubjectPrototype;
import museum.museum.subject.product.FoodProduct;
import museum.player.User;
import museum.util.MessageUtil;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import ru.cristalix.core.GlobalSerializers;
import ru.cristalix.core.util.UtilV3;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static museum.worker.WorkerUtil.defaultSkin;

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
		val spawn = ((StallPrototype) prototype).getSpawn().clone()
				.subtract(prototype.getBox().getCenter().clone().subtract(.5, 4, -.5))
				.add(UtilV3.toVector(cachedInfo.location));
		Npc.npc(new NpcData(
				(int) (Math.random() * Integer.MAX_VALUE),
				UUID.randomUUID(),
				spawn.x,
				spawn.y,
				spawn.z,
				1000,
				"Работница лавки",
				NpcBehaviour.STARE_AT_PLAYER,
				spawn.pitch,
				spawn.yaw,
				defaultSkin,
				defaultSkin.substring(defaultSkin.length() - 10),
				true,
				false,
				false,
				false
		)).show(owner.getPlayer());
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
		owner.depositMoneyWithBooster(key.getCost());
	}
}