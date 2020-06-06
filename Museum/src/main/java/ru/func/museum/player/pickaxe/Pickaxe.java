package ru.func.museum.player.pickaxe;

import lombok.val;
import net.minecraft.server.v1_12_R1.*;
import org.bukkit.craftbukkit.v1_12_R1.CraftWorld;
import ru.func.museum.excavation.Excavation;

import java.util.List;
import java.util.Random;

public interface Pickaxe {

    Random RANDOM = new Random();

    IBlockData AIR_DATA = Block.getById(0).getBlockData();

    World WORLD = ((CraftWorld) Excavation.WORLD).getHandle();

    List<BlockPosition> dig(PlayerConnection connection, Excavation excavation, BlockPosition position);

    default boolean breakBlock(PlayerConnection connection, Excavation excavation, BlockPosition position) {
        if (excavation.getExcavationGenerator().fastCanBreak(position.getX(), position.getY(), position.getZ())) {
            val blockChange = new PacketPlayOutBlockChange(WORLD, position);
            blockChange.block = AIR_DATA;
            connection.sendPacket(blockChange);
            return true;
        }
        return false;
    }

    default void animate(PlayerConnection connection, BlockPosition position) {
        connection.sendPacket(new PacketPlayOutBlockBreakAnimation(
                RANDOM.nextInt(1000),
                position,
                6 + RANDOM.nextInt(3)
        ));
    }
}
