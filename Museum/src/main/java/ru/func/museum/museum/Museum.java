package ru.func.museum.museum;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.minecraft.server.v1_12_R1.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import ru.func.museum.App;
import ru.func.museum.museum.collector.CollectorType;
import ru.func.museum.museum.space.Space;
import ru.func.museum.museum.template.MuseumTemplateType;
import ru.func.museum.player.Archaeologist;
import ru.func.museum.player.pickaxe.Pickaxe;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author func 22.05.2020
 * @project Museum
 */
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Museum implements AbstractMuseum {
    private List<Space> matrix;
    private String title;
    private MuseumTemplateType museumTemplateType;
    private CollectorType collectorType;

    @Override
    public void show(App plugin, Archaeologist archaeologist, Player guest) {
        matrix.forEach(space -> space.show(archaeologist, guest));
        List<Location> locations = museumTemplateType.getMuseumTemplate().getCollectorRoute();
        int[] vertex = new int[]{
                Integer.MIN_VALUE,
                Integer.MAX_VALUE,
                Integer.MIN_VALUE,
                Integer.MAX_VALUE,
        };

        PlayerConnection connection = ((CraftPlayer) guest).getHandle().playerConnection;

        EntityArmorStand armorStand = new EntityArmorStand(Pickaxe.WORLD);
        armorStand.setCustomName("братик, не ругайся!");
        armorStand.id = armorStand.hashCode();
        armorStand.setInvisible(true);
        armorStand.setCustomNameVisible(true);
        armorStand.setPosition(
                locations.get(0).getBlockX() + .5,
                locations.get(0).getBlockY(),
                locations.get(0).getBlockZ() + .5
        );
        armorStand.setNoGravity(true);
        connection.sendPacket(new PacketPlayOutSpawnEntityLiving(armorStand));
        connection.sendPacket(new PacketPlayOutEntityEquipment(
                armorStand.id,
                EnumItemSlot.HEAD,
                CraftItemStack.asNMSCopy(new ItemStack(Material.WORKBENCH))
        ));

        for (Location location : locations) {
            if (location.getBlockX() > vertex[0])
                vertex[0] = location.getBlockX();
            else if (location.getBlockX() < vertex[1])
                vertex[1] = location.getBlockX();
            if (location.getBlockZ() > vertex[2])
                vertex[2] = location.getBlockZ();
            else if (location.getBlockZ() < vertex[3])
                vertex[3] = location.getBlockZ();
            location.getBlock().setType(Material.BEDROCK);
        }

        int dX = vertex[0] - vertex[1];
        int dZ = vertex[2] - vertex[3];
        int p = (dX + dZ) * 2;

        AtomicInteger counter = new AtomicInteger(0);

        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            int dx = 0;
            int dz = 0;
            int angle = 1;

            int count = counter.get();
            if (count < dX) {
                dx = 4000;
                if (count + 1 == dX)
                    angle = 0;
            } else if (count - dX < dZ) {
                dz = 4000;
                if (count - dX + 1 == dZ)
                    angle = 90;
            } else if (count - dX - dZ < dX) {
                dx = -4000;
                if (count - dX - dZ + 1 == dX)
                    angle = 180;
            } else {
                if (count - dX - dZ - dX + 1 == dZ)
                    angle = -90;
                dz = -4000;
            }
            collectorType.getCollector().move(connection, armorStand.id, dx, 0, dz, angle);

            counter.set(counter.incrementAndGet() % p);
        }, 10, 5);
    }
}
