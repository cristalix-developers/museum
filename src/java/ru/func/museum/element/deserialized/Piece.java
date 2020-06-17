package ru.func.museum.element.deserialized;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.server.v1_12_R1.*;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.EulerAngle;
import ru.func.museum.player.pickaxe.Pickaxe;

/**
 * @author func 01.06.2020
 * @project Museum
 */
@Getter
@AllArgsConstructor
public class Piece {
    private double vectorX;
    private double vectorY;
    private double vectorZ;
    private EulerAngle headRotation;
    private Material material;

    public void single(PlayerConnection connection, String title, Location location, Vector3f vector, int noise, int parentId, int subEntity, int id) {
        EntityArmorStand armorStand = new EntityArmorStand(Pickaxe.WORLD);
        armorStand.setCustomName(title);
        armorStand.id = noise * 100_000_000 + parentId * 100_000 + subEntity * 100 + id;
        armorStand.setInvisible(true);
        armorStand.setCustomNameVisible(true);
        armorStand.setPositionRotation(
                location.getBlockX() + Math.abs(vectorX) % 1,
                location.getBlockY() + Math.abs(vectorY) % 1 - 1,
                location.getBlockZ() + Math.abs(vectorZ) % 1,
                location.getYaw(),
                location.getPitch()
        );
        armorStand.setNoGravity(true);
        armorStand.setHeadPose(new Vector3f(
                vector.x + (float) headRotation.getX(),
                vector.y + (float) headRotation.getY(),
                vector.z + (float) headRotation.getZ()
        ));
        connection.sendPacket(new PacketPlayOutSpawnEntityLiving(armorStand));
        connection.sendPacket(new PacketPlayOutEntityEquipment(
                armorStand.id,
                EnumItemSlot.HEAD,
                CraftItemStack.asNMSCopy(new ItemStack(material))
        ));
    }
}
