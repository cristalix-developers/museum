package ru.func.museum.museum.collector;

import lombok.Getter;
import net.minecraft.server.v1_12_R1.PacketPlayOutEntity;
import net.minecraft.server.v1_12_R1.PlayerConnection;

/**
 * @author func 22.05.2020
 * @project Museum
 */
@Getter
public class DefaultCollector implements AbstractCollector {

    private int speed;
    private double cost;

    @Override
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
