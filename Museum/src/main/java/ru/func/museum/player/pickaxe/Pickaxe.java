package ru.func.museum.player.pickaxe;

import lombok.val;
import net.minecraft.server.v1_12_R1.*;
import org.bukkit.craftbukkit.v1_12_R1.CraftWorld;
import ru.cristalix.core.item.Items;
import ru.func.museum.excavation.Excavation;

import java.util.Random;
import java.util.function.Supplier;

public interface Pickaxe {

    Random RANDOM = new Random();

    IBlockData AIR_DATA = Block.getById(0).getBlockData();

    World WORLD = ((CraftWorld) Excavation.WORLD).getHandle();

    void dig(PlayerConnection connection, Excavation excavation, BlockPosition position);

    Supplier<Items.Builder> getItem();

    default void breakBlock(PlayerConnection connection, Excavation excavation, BlockPosition position) {
        if (excavation.getExcavationGenerator().fastCanBreak(position.getX(), position.getY(), position.getZ())) {
            val blockChange = new PacketPlayOutBlockChange(WORLD, position);
            blockChange.block = AIR_DATA;
            connection.sendPacket(blockChange);
        }
    }
}
