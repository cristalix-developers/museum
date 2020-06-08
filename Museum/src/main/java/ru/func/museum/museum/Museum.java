package ru.func.museum.museum;

import lombok.*;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import ru.cristalix.core.item.Items;
import ru.cristalix.core.scoreboard.IScoreboardService;
import ru.func.museum.App;
import ru.func.museum.element.Element;
import ru.func.museum.excavation.Excavation;
import ru.func.museum.museum.coin.AbstractCoin;
import ru.func.museum.museum.hall.Hall;
import ru.func.museum.museum.hall.template.space.Space;
import ru.func.museum.player.Archaeologist;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

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
    @NonNull
    private int spawnX;
    @NonNull
    private int spawnY;
    @NonNull
    private int spawnZ;

    @Override
    public void load(App plugin, Archaeologist archaeologist, Player guest) {
        owner = archaeologist;
        views++;

        updateIncrease();

        IScoreboardService.get().setCurrentObjective(guest.getUniqueId(), "main");

        // Подготовка игрока
        val guestA = plugin.getArchaeologistMap().get(guest.getUniqueId());

        guestA.setCurrentMuseum(this);
        guestA.setCurrentHall(halls.get(0));

        guestA.setCoins(Collections.newSetFromMap(new ConcurrentHashMap<>()));

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

        guest.teleport(new Location(Excavation.WORLD, spawnX, spawnY, spawnZ));
        val connection = ((CraftPlayer) guest).getHandle().playerConnection;

        // Поготовка заллов
        halls.forEach(hall -> {
            hall.getMatrix().forEach(space -> space.show(archaeologist, guest));
            hall.generateCollector(connection);
        });
    }

    @Override
    public void unload(App app, Archaeologist archaeologist, Player guest) {
        val connection = ((CraftPlayer) guest).getHandle().playerConnection;
        // Очстка витрин, коллекторов
        halls.forEach(hall -> {
            hall.getMatrix().forEach(space -> space.hide(archaeologist, guest));
            hall.removeCollector(connection);
        });
        // Очистка монет
        Set<AbstractCoin> coins = app.getArchaeologistMap().get(guest.getUniqueId()).getCoins();
        coins.forEach(coin -> coin.remove(connection));
        coins.clear();
    }

    @Override
    public void updateIncrease() {
        summaryIncrease = .1;
        for (Hall hall : halls)
            for (Space space : hall.getMatrix())
                for (Element element : space.getElements())
                    summaryIncrease += element.getIncrease();
    }
}
