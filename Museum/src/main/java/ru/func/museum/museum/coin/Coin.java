package ru.func.museum.museum.coin;

import lombok.Getter;
import lombok.val;
import net.minecraft.server.v1_12_R1.*;
import org.bukkit.SoundCategory;
import org.bukkit.*;
import org.bukkit.entity.Player;
import ru.func.museum.App;
import ru.func.museum.player.Archaeologist;
import ru.func.museum.player.pickaxe.Pickaxe;

/**
 * @author func 08.06.2020
 * @project Museum
 */
public class Coin implements AbstractCoin {

    private EntityItem entityItem;
    private Location location;
    @Getter
    private long timestamp;

    public Coin(Location location) {
        this.location = location;
        entityItem = new EntityItem(Pickaxe.WORLD, location.getX(), location.getY(), location.getZ(), COIN);
        entityItem.id = Pickaxe.RANDOM.nextInt(200) + 600;
        timestamp = System.currentTimeMillis();
    }

    @Override
    public void remove(PlayerConnection connection) {
        connection.sendPacket(new PacketPlayOutEntityDestroy(entityItem.getId()));
    }

    @Override
    public void create(PlayerConnection connection) {
        connection.sendPacket(new PacketPlayOutSpawnEntity(entityItem, 2));
        connection.sendPacket(new PacketPlayOutEntityMetadata(entityItem.getId(), entityItem.getDataWatcher(), false));
    }

    @Override
    public boolean pickUp(Player player, Archaeologist archaeologist, Location location, double radius) {
        boolean close = this.location.distanceSquared(location) <= radius * radius;

        if (close) {
            entityItem.setCustomNameVisible(true);
            entityItem.setNoGravity(true);

            // Расчет стоимости монеты
            val money = (archaeologist.getCurrentMuseum().getSummaryIncrease() / archaeologist.getMuseumList().size()) * (.5 + Pickaxe.RANDOM.nextDouble());
            val format = Math.floor(money * 100) / 100;
            entityItem.setCustomName("§6+ " + format + "$");

            val connection = archaeologist.getConnection();

            connection.sendPacket(new PacketPlayOutEntityVelocity(entityItem.getId(), 0, .05, 0));
            connection.sendPacket(new PacketPlayOutEntityMetadata(entityItem.getId(), entityItem.getDataWatcher(), false));

            archaeologist.incPickedCoinsCount();

            Bukkit.getScheduler().runTaskLaterAsynchronously(App.getApp(), () -> {
                remove(connection);
                player.playSound(this.location, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.WEATHER, .2F, 1);
                player.spawnParticle(Particle.TOTEM, this.location.add(0, 1.5, 0), 5, 0, 0, 0, .3);
                archaeologist.setMoney(archaeologist.getMoney() + money);
            }, 30);
        }

        return close;
    }
}
