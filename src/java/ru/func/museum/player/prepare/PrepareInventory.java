package ru.func.museum.player.prepare;

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

    private ItemStack menu = Items.builder()
            .type(Material.PAPER)
            .displayName("§f>> §l§6Меню §f<<")
            .loreLines(
                    "",
                    "§7Это меню, с помощью которого,",
                    "§7настраивать музей, приглашать",
                    "§7друзей, а так же смотреть",
                    "§7подробную статистику."
            ).build();

    private ItemStack excavations = Items.builder()
            .type(Material.EMERALD)
            .displayName("§f>> §l§6Раскопки §f<<")
            .loreLines(
                    "",
                    "§7Нажмите правую кнопку",
                    "§7мыши, что бы открыть",
                    "§7меню, где вы можете",
                    "§7выбирать экспидицию и кирку"
            ).build();

    @Override
    public void execute(Player player, User archaeologist, App app) {
        val inventory = player.getInventory();
        inventory.clear();
        inventory.setItem(0, menu);
        inventory.setItem(1, excavations);
    }
}
