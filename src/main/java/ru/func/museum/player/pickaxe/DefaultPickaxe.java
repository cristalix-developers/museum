package ru.func.museum.player.pickaxe;

import net.minecraft.server.v1_12_R1.BlockPosition;
import net.minecraft.server.v1_12_R1.PacketPlayOutBlockBreakAnimation;
import net.minecraft.server.v1_12_R1.PlayerConnection;
import org.bukkit.block.Block;
import ru.func.museum.excavation.Excavation;

/**
 * @author func 24.05.2020
 * @project Museum
 */
public class DefaultPickaxe implements Pickaxe {

    @Override
    public void dig(PlayerConnection connection, Excavation excavation, Block block) {
        BlockPosition blockPosition = new BlockPosition(block.getX(), block.getY(), block.getZ());
        connection.sendPacket(new PacketPlayOutBlockBreakAnimation(
                block.hashCode(),
                RANDOM.nextBoolean() ? blockPosition.east() : blockPosition.north(),
                7
        ));
    }
}
