package ru.cristalix.museum.player.prepare;

import clepto.bukkit.Lemonade;
import lombok.val;
import org.bukkit.inventory.ItemStack;
import ru.cristalix.museum.App;
import ru.cristalix.museum.player.User;

/**
 * @author func 03.06.2020
 * @project Museum
 */
public class PrepareInventory implements Prepare {

	private final ItemStack menu = Lemonade.get("menu").render();

	@Override
	public void execute(User user, App app) {
		val inventory = user.getPlayer().getInventory();
		inventory.clear();
		inventory.setItem(0, menu);
	}

}
