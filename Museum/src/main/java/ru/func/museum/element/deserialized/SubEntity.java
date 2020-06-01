package ru.func.museum.element.deserialized;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.server.v1_12_R1.*;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;
import ru.func.museum.player.pickaxe.Pickaxe;

import java.util.List;

/**
 * @author func 31.05.2020
 * @project Museum
 */
@Getter
@AllArgsConstructor
public class SubEntity {
    private List<Piece> pieces;
    private String title;

    public void show(PlayerConnection connection, Location location, int parentId, int subEntity) {
        int i = 0;

        float randomXAngle = Pickaxe.RANDOM.nextFloat() * 360;
        float randomYAngle = Pickaxe.RANDOM.nextFloat() * 360;
        float randomZAngle = Pickaxe.RANDOM.nextFloat() * 360;

        int noise = 1 + Pickaxe.RANDOM.nextInt(9);
        for(Piece piece : pieces) {
            EntityArmorStand armorStand = new EntityArmorStand(Pickaxe.WORLD);
            armorStand.setCustomName(title);
            armorStand.id = noise * 100_000_000 + parentId * 100_000 + subEntity * 100 + i;
            armorStand.setInvisible(true);
            armorStand.setCustomNameVisible(true);
            armorStand.setPosition(
                    location.getBlockX() + Math.abs(piece.getVectorX()) % 1,
                    location.getBlockY() + Math.abs(piece.getVectorY()) % 1 - 1,
                    location.getBlockZ() + Math.abs(piece.getVectorZ()) % 1
            );
            armorStand.setNoGravity(true);
            armorStand.setHeadPose(new Vector3f(
                    (float) piece.getHeadRotation().getX() + randomXAngle,
                    (float) piece.getHeadRotation().getY() + randomYAngle,
                    (float) piece.getHeadRotation().getZ() + randomZAngle
            ));
            connection.sendPacket(new PacketPlayOutSpawnEntityLiving(armorStand));
            connection.sendPacket(new PacketPlayOutEntityEquipment(
                    armorStand.id,
                    EnumItemSlot.HEAD,
                    CraftItemStack.asNMSCopy(new ItemStack(piece.getMaterial()))
            ));
            i++;
        }
    }
}
