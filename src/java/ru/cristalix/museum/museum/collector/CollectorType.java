package ru.cristalix.museum.museum.collector;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.server.v1_12_R1.*;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

@Getter
@AllArgsConstructor
public enum CollectorType {
    NONE("отсутствует", 			       new ItemStack(Material.PAPER), 0, 0, 0,  -10),
    AMATEUR("любительский", 		   new ItemStack(Material.WORKBENCH), 1, 100_000, 1.5, -1),
    PROFESSIONAL("профессиольный",  new ItemStack(Material.WORKBENCH), 2, 400_000, 2,69),
    PRESTIGE("престижный",		   new ItemStack(Material.WORKBENCH), 4, 750_000, 3, 99);

    private final String name;
    private final ItemStack head;
    private final int speed;
    private final int cost;
    private final double radius;
    private final int cristalixCost;

    public void move(PlayerConnection connection, EntityArmorStand armorStand, double dx, double dy, double dz, float yaw, float pitch) {
        connection.sendPacket(new PacketPlayOutEntity.PacketPlayOutRelEntityMoveLook(
                armorStand.getId(),
                (short) (4096 * dx), (short) (4096 * dy), (short) (4096 * dz),
                (byte) (yaw * 256 / 360), (byte) (pitch * 256 / 360),
                false
        ));
    /*    armorStand.setHeadPose(new Vector3f(pitch, 0, 0));
        connection.sendPacket(new PacketPlayOutEntityMetadata(armorStand.getId(), armorStand.getDataWatcher(), false));*/
    }
}
