package ru.func.museum.player.pickaxe;

import net.minecraft.server.v1_12_R1.Block;
import net.minecraft.server.v1_12_R1.IBlockData;
import net.minecraft.server.v1_12_R1.World;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_12_R1.CraftWorld;
import org.bukkit.entity.Player;
import ru.func.museum.excavation.Excavation;

import java.util.Random;

public interface Pickaxe {

    Random RANDOM = new Random();

    IBlockData AIR_DATA = Block.getById(0).getBlockData();

    World WORLD = ((CraftWorld) Excavation.WORLD).getHandle();

    Location[] dig(Player player, org.bukkit.block.Block block);
}
