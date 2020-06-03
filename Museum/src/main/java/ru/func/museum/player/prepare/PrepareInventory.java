package ru.func.museum.player.prepare;

import lombok.val;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import ru.cristalix.core.item.Items;
import ru.func.museum.App;
import ru.func.museum.player.Archaeologist;

/**
 * @author func 03.06.2020
 * @project Museum
 */
public class PrepareInventory implements Prepare {

    private ItemStack menu = Items.builder()
            .type(Material.PAPER)
            .displayName("§f>> §l§6Меню [§fПКМ§6] §f<<")
            .loreLines(
                    "",
                    "§7Это меню, с помощью которого,",
                    "§7вы можете выбирать экспидицию,",
                    "§7настраивать музей, приглашать",
                    "§7друзей, а так же смотреть",
                    "§7подробную статистику."
            ).build();

    @Override
    public void execute(Player player, Archaeologist archaeologist, App app) {
        val inventory = player.getInventory();
        inventory.clear();
        inventory.setItem(0, menu);
        inventory.setItem(1, archaeologist.getPickaxeType().getPickaxe()
                .getItem()
                .get()
                .displayName("§f>> §l§6Кирки [§fПКМ§6] §f<<")
                .loreLines("", "§7Нажмите правую кнопку", "§7мыши, что бы открыть", "§7меню с магазином.")
                .build());
    }
}
