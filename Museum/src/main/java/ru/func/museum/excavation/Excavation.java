package ru.func.museum.excavation;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import ru.cristalix.core.scoreboard.IScoreboardService;
import ru.func.museum.excavation.generator.ExcavationGenerator;
import ru.func.museum.player.Archaeologist;
import ru.func.museum.util.MessageUtil;

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
        player.getInventory().clear();
        player.getInventory().addItem(archaeologist.getPickaxeType().getItem());
        player.teleport(excavation.getStartLocation());

        IScoreboardService.get().setCurrentObjective(player.getUniqueId(), "excavation");

        player.sendTitle("§6Прибытие!", getTitle());

        MessageUtil.find("visitexcavation")
                .set("title", getTitle())
                .send(player);
        excavation.getExcavationGenerator().generateAndShow(player);
    }
}
