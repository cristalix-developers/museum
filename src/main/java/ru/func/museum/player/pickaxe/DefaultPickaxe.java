package ru.func.museum.player.pickaxe;

import net.minecraft.server.v1_12_R1.BlockPosition;
import net.minecraft.server.v1_12_R1.PacketPlayOutBlockBreakAnimation;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

/**
 * @author func 24.05.2020
 * @project Museum
 */
public class DefaultPickaxe implements Pickaxe {
    @Override
    public Location[] dig(Player player, Block block) {
        BlockPosition blockPosition = new BlockPosition(block.getX(), block.getY(), block.getZ());
        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(new PacketPlayOutBlockBreakAnimation(
                block.hashCode(),
                RANDOM.nextBoolean() ? blockPosition.east() : blockPosition.north(),
                7
        ));
        return null;
    }
}
