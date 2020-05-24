package ru.func.museum.player.pickaxe;

import net.minecraft.server.v1_12_R1.BlockPosition;
import net.minecraft.server.v1_12_R1.PacketPlayOutBlockBreakAnimation;
import net.minecraft.server.v1_12_R1.PlayerConnection;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

/**
 * @author func 24.05.2020
 * @project Museum
 */
public class RarePickaxe implements Pickaxe {
    @Override
    public Location[] dig(Player player, Block block) {
        BlockPosition blockPosition = new BlockPosition(block.getX(), block.getY(), block.getZ());
        PlayerConnection connection = ((CraftPlayer) player).getHandle().playerConnection;
        connection.sendPacket(generateAnimation(blockPosition.east()));
        connection.sendPacket(generateAnimation(blockPosition.north()));
        connection.sendPacket(generateAnimation(blockPosition.down()));
        connection.sendPacket(generateAnimation(blockPosition.south()));
        connection.sendPacket(generateAnimation(blockPosition.west()));
        return new Location[]{block.getLocation().subtract(
                RANDOM.nextInt(3) - 1,
                0,
                RANDOM.nextInt(3) - 1
        )};
    }

    private PacketPlayOutBlockBreakAnimation generateAnimation(BlockPosition blockPosition) {
        return new PacketPlayOutBlockBreakAnimation(
                blockPosition.hashCode() + RANDOM.nextInt(11), // chance to hide animation higher if number smaller
                blockPosition,
                4 + RANDOM.nextInt(5)
        );
    }
}
