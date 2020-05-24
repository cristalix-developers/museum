package ru.func.museum.player.pickaxe;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

/**
 * @author func 24.05.2020
 * @project Museum
 */
public class UltraPickaxe implements Pickaxe {
    @Override
    public Location[] dig(Player player, Block block) {
        return new Location[]{
                block.getLocation().subtract(0, 0, 1),
                block.getLocation().subtract(0, 0, -1),
                block.getLocation().subtract(1, 0, 0),
                block.getLocation().subtract(-1, 0, 0),
        };
    }
}
