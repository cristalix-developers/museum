package ru.func.museum.museum;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.server.v1_12_R1.PacketPlayOutEntity;
import net.minecraft.server.v1_12_R1.PlayerConnection;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

@Getter
@AllArgsConstructor
public enum CollectorType {
    NONE("отсутствует", new ItemStack(Material.PAPER), 0, 0, -10),
    LOVED("любительский", new ItemStack(Material.WORKBENCH), 5, 100_000, -1),
    PROFESSIONAL("профессиольный", new ItemStack(Material.WORKBENCH), 10, 400_000, 69),
    PRESTIGE("престижный", new ItemStack(Material.WORKBENCH), 15, 750_000, 99);

    private String name;
    private ItemStack head;
    private int speed;
    private int cost;
    private int cristalixCost;

    public void move(PlayerConnection connection, int id, int dx, int dy, int dz, int angle) {
        connection.sendPacket(new PacketPlayOutEntity.PacketPlayOutRelEntityMove(
                id, dx, dy, dz, false
        ));
        if (angle % 90 == 0) {
            connection.sendPacket(new PacketPlayOutEntity.PacketPlayOutRelEntityMoveLook(
                    id, 0, 0, 0, (byte) (angle * 256 / 360), (byte) 0, true
            ));
        }
    }
}
