package ru.func.museum.excavation.generator;

import net.minecraft.server.v1_12_R1.*;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_12_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import ru.func.museum.player.pickaxe.Pickaxe;

/**
 * @author func 22.05.2020
 * @project Museum
 */
public class HalfSphereGenerator implements ExcavationGenerator {

    private Location center;
    private int radius;
    private Location[] blockAble;
    private Material[] ableBlockType;
    private int[] ableEntity;
    private World world;

    public HalfSphereGenerator(Location center, int radius, Material[] ableBlockType, int... ableEntity) {
        this.center = center;
        this.radius = radius;
        this.ableBlockType = ableBlockType;
        this.ableEntity = ableEntity;
        world = ((CraftWorld) center.getWorld()).getHandle();

        int index = 0;
        Location[] temp = new Location[(int) (4 * radius * radius * radius * Math.PI / 3 - 3 * radius * radius + 3 * radius - 1) / 2];
        for (int y = -radius; y < 0; y++) {
            for (int x = -radius; x < radius; x++) {
                for (int z = -radius; z < radius; z++) {
                    if (index == temp.length)
                        break;
                    if (y + center.getBlockY() < 1)
                        continue;
                    if (fastCanBreak(center.getBlockX() + x, center.getBlockY() + y, center.getBlockZ() + z)) {
                        Location tempLocation = center.subtract(x, -y, z);
                        temp[index] = new Location(
                                tempLocation.getWorld(),
                                tempLocation.getX(),
                                tempLocation.getY(),
                                tempLocation.getZ()
                        );
                        tempLocation.getBlock().setType(Material.AIR);
                        center.subtract(-x, y, -z);
                        index++;
                    }
                }
            }
        }
        blockAble = new Location[index];
        System.arraycopy(temp, 0, blockAble, 0, index);
    }

    @Override
    public int[] getElementsId() {
        return ableEntity;
    }

    @Override
    public void generateAndShow(Player player) {
        PlayerConnection connection = ((CraftPlayer) player).getHandle().playerConnection;
        for (Location location : blockAble) {
            PacketPlayOutBlockChange block = new PacketPlayOutBlockChange(
                    world,
                    new BlockPosition(
                            location.getX(),
                            location.getY(),
                            location.getZ()
                    )
            );
            Material material = ableBlockType[Pickaxe.RANDOM.nextInt(ableBlockType.length)];
            block.block = Block.getByCombinedId(material.getId());
            connection.sendPacket(block);
        }
    }

    @Override
    public boolean fastCanBreak(int x, int y, int z) {
        return (center.getBlockX() - x) * (center.getBlockX() - x) +
                (center.getBlockY() - y) * (center.getBlockY() - y) +
                (center.getBlockZ() - z) * (center.getBlockZ() - z)
                <= radius * radius;
    }
}
