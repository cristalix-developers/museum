package ru.func.museum.player.prepare;

import clepto.bukkit.Lemonade;
import lombok.val;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import ru.cristalix.core.item.Items;
import ru.func.museum.App;
import ru.func.museum.player.User;

/**
 * @author func 03.06.2020
 * @project Museum
 */
public class PrepareInventory implements Prepare {

    private ItemStack menu = Lemonade.get("menu").render();
    private ItemStack excavations = Lemonade.get("excavations").render();

    @Override
    public void execute(User user, App app) {
        val inventory = user.getPlayer().getInventory();
        inventory.clear();
        inventory.setItem(0, menu);
        inventory.setItem(1, excavations);
    }
}
