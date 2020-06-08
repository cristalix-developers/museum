package ru.func.museum.museum.coin;

import net.minecraft.server.v1_12_R1.*;
import org.bukkit.Location;
import ru.func.museum.player.Archaeologist;
import ru.func.museum.player.pickaxe.Pickaxe;

/**
 * @author func 08.06.2020
 * @project Museum
 */
public class Coin implements AbstractCoin {

    private int id;
    private Location location;

    public Coin(Location location) {
        this.id = Pickaxe.RANDOM.nextInt(200) + 600;
        this.location = location;
    }

    @Override
    public void remove(PlayerConnection connection) {
        connection.sendPacket(new PacketPlayOutEntityDestroy(id));
    }

    @Override
    public void create(PlayerConnection connection) {
        EntityItem item = new EntityItem(Pickaxe.WORLD, location.getX(), location.getY(), location.getZ(), COIN);
        item.setCustomNameVisible(true);
        item.id = id;

        connection.sendPacket(new PacketPlayOutSpawnEntity(item, 2));
        connection.sendPacket(new PacketPlayOutEntityMetadata(id, item.getDataWatcher(), false));
    }

    @Override
    public boolean pickUp(PlayerConnection connection, Archaeologist archaeologist, Location location, double radius) {
        boolean close = this.location.distanceSquared(location) <= radius * radius;

        if (close) {
            remove(connection);
            archaeologist.setMoney(archaeologist.getMoney() + 1);
        }

        return close;
    }
}
