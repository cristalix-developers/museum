package ru.func.museum.listener;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import ru.cristalix.core.inventory.ControlledInventory;
import ru.cristalix.core.inventory.InventoryContents;
import ru.cristalix.core.inventory.InventoryProvider;
import ru.func.museum.App;
import ru.func.museum.player.Archaeologist;

/**
 * @author func 04.06.2020
 * @project Museum
 */
public class MuseumItemHandler implements Listener {

    private ControlledInventory museumUI = ControlledInventory.builder()
            .provider(new InventoryProvider() {
                @Override
                public void init(Player player, InventoryContents contents) {
                    Archaeologist archaeologist = App.getApp().getArchaeologistMap().get(player.getUniqueId());
                    contents.resetMask("");
                }
            }).title("§bМузей")
            .rows(1)
            .type(InventoryType.CHEST)
            .build();


    @EventHandler
    public void onItemClick(PlayerInteractEvent e) {
        if (e.getItem() != null && !e.getMaterial().equals(Material.AIR)) {
            Material material = e.getMaterial();
            if (material.equals(Material.PAPER)) {
                // меню
            } else if (material.name().contains("PICKAXE")) {

            }
        }
    }
}
