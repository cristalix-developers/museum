package museum.command;

import clepto.bukkit.B;
import lombok.val;
import museum.App;
import museum.util.MessageUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * @author func 16.10.2020
 * @project museum
 */
public class WagonCommand {

	public static final int WAGON_COST = 500;
	private final List<UUID> playerOrderedWagon = new ArrayList<>();

	public WagonCommand(App app) {
		B.regCommand((sender, args) -> {
			if (playerOrderedWagon.contains(sender.getUniqueId())) {
				// выдать коробку
				playerOrderedWagon.remove(sender.getUniqueId());
			}
			return null;
		}, "wagon");
		B.regCommand((sender, args) -> {
			val user = app.getUser(sender);
			if (user.getMoney() < WAGON_COST)
				return MuseumCommands.NO_MONEY_MESSAGE;
			user.setMoney(user.getMoney() - WAGON_COST);
			playerOrderedWagon.add(sender.getUniqueId());
			return MessageUtil.get("wagon-buy");
		}, "wagonbuy");
	}
}
