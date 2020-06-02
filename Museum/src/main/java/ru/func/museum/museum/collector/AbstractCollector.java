package ru.func.museum.museum.collector;

import net.minecraft.server.v1_12_R1.PlayerConnection;

public interface AbstractCollector {

    int getSpeed();

    double getCost();

    void move(PlayerConnection connection, int id, int dx, int dy, int dz, int angle);
}
