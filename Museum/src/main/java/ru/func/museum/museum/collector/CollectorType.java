package ru.func.museum.museum.collector;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.server.v1_12_R1.*;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

@Getter
@AllArgsConstructor
public enum CollectorType {
    NONE("отсутствует", new ItemStack(Material.PAPER), 0, 0, -10),
    LOVED("любительский", new ItemStack(Material.WORKBENCH), 1, 100_000, -1),
    PROFESSIONAL("профессиольный", new ItemStack(Material.WORKBENCH), 2, 400_000, 69),
    PRESTIGE("престижный", new ItemStack(Material.WORKBENCH), 4, 750_000, 99);

    private String name;
    private ItemStack head;
    private int speed;
    private int cost;
    private int cristalixCost;

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
