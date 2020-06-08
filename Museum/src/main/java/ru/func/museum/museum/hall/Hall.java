package ru.func.museum.museum.hall;

import lombok.*;
import net.minecraft.server.v1_12_R1.*;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import ru.func.museum.excavation.Excavation;
import ru.func.museum.museum.collector.CollectorNavigator;
import ru.func.museum.museum.collector.CollectorType;
import ru.func.museum.museum.hall.template.HallTemplateType;
import ru.func.museum.museum.hall.template.space.Space;
import ru.func.museum.player.Archaeologist;
import ru.func.museum.player.pickaxe.Pickaxe;

import java.util.List;

/**
 * @author func 07.06.2020
 * @project Museum
 */
@Getter
@Setter
@RequiredArgsConstructor
@NoArgsConstructor
public class Hall {
    @NonNull
    private List<Space> matrix;
    @NonNull
    private HallTemplateType hallTemplateType;
    @NonNull
    private CollectorType collectorType;
    private transient EntityArmorStand armorStand;
    private transient Location previousLocation;
    private transient CollectorNavigator navigator;

    public void moveCollector(Archaeologist archaeologist, Player player, long iteration) {
        if (collectorType.equals(CollectorType.NONE))
            return;

        val location = getLocation(iteration);

        archaeologist.getCoins().removeIf(coin -> coin.pickUp(player, archaeologist, location, collectorType.getRadius()));

        collectorType.move(
                ((CraftPlayer) player).getHandle().playerConnection,
                armorStand,
                location.getX() - previousLocation.getX(),
                location.getY() - previousLocation.getY(),
                location.getZ() - previousLocation.getZ(),
                location.getYaw(),
                location.getPitch()
        );
        previousLocation = location;
    }

    public void removeCollector(PlayerConnection connection) {
        connection.sendPacket(new PacketPlayOutEntityDestroy(armorStand.getId()));
    }

    public void generateCollector(PlayerConnection connection) {
        val armorStand = new EntityArmorStand(Pickaxe.WORLD);
        val endpoints = hallTemplateType.getHallTemplate().getCollectorRoute();

        navigator = new CollectorNavigator(Excavation.WORLD, endpoints);

        val location = getLocation(System.currentTimeMillis());

        armorStand.setCustomName("§6Коллектор " + collectorType.getName());
        armorStand.id = 800 + Pickaxe.RANDOM.nextInt(200);
        armorStand.setInvisible(true);
        armorStand.setCustomNameVisible(true);
        armorStand.setPosition(
                location.getX(),
                location.getY(),
                location.getZ()
        );
        armorStand.setNoGravity(true);
        connection.sendPacket(new PacketPlayOutSpawnEntityLiving(armorStand));
        connection.sendPacket(new PacketPlayOutEntityEquipment(
                armorStand.id,
                EnumItemSlot.HEAD,
                CraftItemStack.asNMSCopy(collectorType.getHead())
        ));
        this.armorStand = armorStand;
        previousLocation = location;
    }

    private Location getLocation(long time) {
        return navigator.getLocation(time * collectorType.getSpeed() % 25_000 / 25_000D);
    }

    public boolean isInside(Location location) {
        return hallTemplateType.getHallTemplate().isInside(location);
    }
}
