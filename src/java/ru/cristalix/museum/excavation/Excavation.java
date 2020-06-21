package ru.cristalix.museum.excavation;

import clepto.bukkit.Lemonade;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.bukkit.entity.Player;
import ru.cristalix.core.scoreboard.IScoreboardService;
import ru.cristalix.museum.player.User;
import ru.cristalix.museum.util.MessageUtil;

@Data
@AllArgsConstructor
public class Excavation {

	private final ExcavationPrototype prototype;
	private int hitsLeft;

	public void load(User user) {
		Player player = user.getPlayer();
		player.getInventory().clear();
		player.getInventory().addItem(Lemonade.get("pickaxe-" + user.getPickaxeType().name().toLowerCase()).render());
		player.teleport(prototype.getSpawnPoint());

		IScoreboardService.get().setCurrentObjective(user.getUuid(), "excavation");

		String title = prototype.getTitle();
		player.sendTitle("§6Прибытие!", title);

		MessageUtil.find("visitexcavation")
				.set("title", title)
				.send(user);

		prototype.getPackets().forEach(user::sendPacket);
	}

}
