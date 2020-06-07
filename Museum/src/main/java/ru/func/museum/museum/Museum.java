package ru.func.museum.museum;

import lombok.*;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import ru.cristalix.core.item.Items;
import ru.cristalix.core.scoreboard.IScoreboardService;
import ru.func.museum.App;
import ru.func.museum.element.Element;
import ru.func.museum.museum.hall.Hall;
import ru.func.museum.museum.space.Space;
import ru.func.museum.player.Archaeologist;

import java.util.Date;
import java.util.List;

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
    private List<Hall> halls;
    private transient double summaryIncrease;
    @NonNull
    private String title;

    @Override
    public void load(App plugin, Archaeologist archaeologist, Player guest) {
        owner = archaeologist;
        views++;

        updateIncrease();

        IScoreboardService.get().setCurrentObjective(guest.getUniqueId(), "main");

        // Подготовка игрока
        val guestA = plugin.getArchaeologistMap().get(guest.getUniqueId());
        guestA.setCurrentMuseum(this);

        guest.getInventory().remove(Material.SADDLE);

        if (!guestA.equals(owner)) {
            guest.getInventory().setItem(8, Items.builder()
                    .type(Material.SADDLE)
                    .displayName("§bВернуться")
                    .loreLines(
                            "",
                            "§7Нажмите ПКМ, что бы вернуться."
                    ).build()
            );
        }

        guest.teleport(halls.get(0).getHallTemplateType().getHallTemplate().getSpawn());
        val connection = ((CraftPlayer) guest).getHandle().playerConnection;

        // Поготовка заллов
        halls.forEach(hall -> {
            hall.getMatrix().forEach(space -> space.show(archaeologist, guest));
            hall.generateCollector(connection);
        });

        val id = guestA.getCurrentMuseum().getDate();

        new BukkitRunnable() {

            int counter = 0;

            @Override
            public void run() {
                if (!guest.isOnline() || archaeologist.isOnExcavation() || !id.equals(guestA.getCurrentMuseum().getDate()))
                    cancel();
                halls.forEach(hall -> hall.moveCollector(connection, counter));

                counter = ++counter % 500;
            }
        }.runTaskTimerAsynchronously(plugin, 0, 1);
    }

    @Override
    public void unload(Archaeologist archaeologist, Player guest) {
        val connection = ((CraftPlayer) guest).getHandle().playerConnection;
        halls.forEach(hall -> {
            hall.getMatrix().forEach(space -> space.hide(archaeologist, guest));
            hall.removeCollector(connection);
        });
    }

    @Override
    public void updateIncrease() {
        summaryIncrease = 0;
        for (Hall hall : halls)
            for (Space space : hall.getMatrix())
                for (Element element : space.getElements())
                    summaryIncrease += element.getIncrease();
    }
}
