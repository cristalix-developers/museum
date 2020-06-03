package ru.func.museum.excavation;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import ru.func.museum.excavation.generator.ExcavationGenerator;
import ru.func.museum.player.Archaeologist;

public interface Excavation {

    World WORLD = Bukkit.getWorld("world");

    ExcavationGenerator getExcavationGenerator();
    String getTitle();
    double getCost();
    int getMinimalLevel();
    Location getStartLocation();
    int getBreakCount();

    default void load(Archaeologist archaeologist, Player player) {
        Excavation excavation = archaeologist.getLastExcavation()
                .getExcavation();
        player.teleport(excavation.getStartLocation());
        player.sendTitle("§6Прибытие!", getTitle());
        player.sendMessage("§7[§l§bi§7] Вы прибыли на §l§b" + getTitle() + ".");
        excavation.getExcavationGenerator().generateAndShow(player);
    }
}
