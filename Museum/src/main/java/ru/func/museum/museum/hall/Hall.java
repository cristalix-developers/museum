package ru.func.museum.museum.hall;

import lombok.*;
import net.minecraft.server.v1_12_R1.*;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack;
import ru.func.museum.excavation.Excavation;
import ru.func.museum.museum.collector.CollectorNavigator;
import ru.func.museum.museum.collector.CollectorType;
import ru.func.museum.museum.hall.template.HallTemplateType;
import ru.func.museum.museum.space.Space;
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

    public void moveCollector(Archaeologist archaeologist, PlayerConnection connection, double iteration) {
        if (collectorType.equals(CollectorType.NONE))
            return;

        iteration = iteration / 500 * collectorType.getSpeed();

        val location = navigator.getLocation(iteration);

        archaeologist.getCoins().removeIf(coin -> coin.pickUp(connection, archaeologist, location, collectorType.getRadius()));

        collectorType.move(
                connection,
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

        armorStand.setCustomName("§6Коллектор " + collectorType.getName());
        armorStand.id = 800 + Pickaxe.RANDOM.nextInt(200);
        armorStand.setInvisible(true);
        armorStand.setCustomNameVisible(true);
        armorStand.setPosition(
                endpoints.get(0).getBlockX(),
                endpoints.get(0).getBlockY(),
                endpoints.get(0).getBlockZ()
        );
        armorStand.setNoGravity(true);
        connection.sendPacket(new PacketPlayOutSpawnEntityLiving(armorStand));
        connection.sendPacket(new PacketPlayOutEntityEquipment(
                armorStand.id,
                EnumItemSlot.HEAD,
                CraftItemStack.asNMSCopy(collectorType.getHead())
        ));
        this.armorStand = armorStand;
        previousLocation = armorStand.getBukkitEntity().getLocation();
    }

    public boolean isInside(Location location) {
        return hallTemplateType.getHallTemplate().isInside(location);
    }
}
