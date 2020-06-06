package ru.func.museum.museum;

import lombok.*;
import net.minecraft.server.v1_12_R1.*;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import ru.cristalix.core.item.Items;
import ru.cristalix.core.scoreboard.IScoreboardService;
import ru.func.museum.App;
import ru.func.museum.element.Element;
import ru.func.museum.museum.space.Space;
import ru.func.museum.museum.template.MuseumTemplateType;
import ru.func.museum.player.Archaeologist;
import ru.func.museum.player.pickaxe.Pickaxe;

import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author func 22.05.2020
 * @project Museum
 */
@Setter
@Getter
@RequiredArgsConstructor
@NoArgsConstructor
public class Museum implements AbstractMuseum {
    @NonNull
    private Date date;
    private long views;
    private transient Archaeologist owner;
    @NonNull
    private List<Space> matrix;
    @NonNull
    private String title;
    @NonNull
    private MuseumTemplateType museumTemplateType;
    @NonNull
    private CollectorType collectorType;
    private transient double summaryIncrease;

    @Override
    public void load(App plugin, Archaeologist archaeologist, Player guest) {
        owner = archaeologist;
        views++;

        updateIncrease();

        IScoreboardService.get().setCurrentObjective(guest.getUniqueId(), "main");

        matrix.forEach(space -> space.show(archaeologist, guest));
        guest.teleport(museumTemplateType.getMuseumTemplate().getSpawn());

        int[] vertex = new int[]{
                Integer.MIN_VALUE,
                Integer.MAX_VALUE,
                Integer.MIN_VALUE,
                Integer.MAX_VALUE,
        };

        val guestArchaeologist = plugin.getArchaeologistMap().get(guest.getUniqueId());
        guestArchaeologist.setCurrentMuseum(this);

        guest.getInventory().remove(Material.SADDLE);

        if (!guestArchaeologist.equals(owner)) {
            guest.getInventory().setItem(8, Items.builder()
                    .type(Material.SADDLE)
                    .displayName("§bВернуться")
                    .loreLines(
                            "",
                            "§7Нажмите ПКМ, что бы вернуться."
                    ).build()
            );
        }

        if (collectorType.equals(CollectorType.NONE))
            return;

        val locations = museumTemplateType.getMuseumTemplate().getCollectorRoute();
        val connection = ((CraftPlayer) guest).getHandle().playerConnection;
        val armorStand = new EntityArmorStand(Pickaxe.WORLD);

        armorStand.setCustomName("§6Коллектор " + collectorType.getName());
        armorStand.id = 999;
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
                CraftItemStack.asNMSCopy(collectorType.getHead())
        ));

        for (val location : locations) {
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

        val counter = new AtomicInteger(0);
        val id = guestArchaeologist.getCurrentMuseum();

        new BukkitRunnable() {
            @Override
            public void run() {
                if (!guest.isOnline() || archaeologist.isOnExcavation() || id != guestArchaeologist.getCurrentMuseum())
                    cancel();

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
                collectorType.move(connection, armorStand.id, dx, 0, dz, angle);

                counter.set(counter.incrementAndGet() % p);
            }
        }.runTaskTimerAsynchronously(plugin, 5, 20 - collectorType.getSpeed());
    }

    @Override
    public void unload(App plugin, Archaeologist archaeologist, Player guest) {
        matrix.forEach(space -> space.hide(archaeologist, guest));
        ((CraftPlayer) guest).getHandle().playerConnection.sendPacket(new PacketPlayOutEntityDestroy(999));
    }

    @Override
    public void updateIncrease() {
        summaryIncrease = 0;
        for (Space space : matrix)
            for (Element element : space.getElements())
                summaryIncrease += element.getIncrease();
    }
}
