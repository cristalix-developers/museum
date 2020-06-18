package ru.func.museum.excavation;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import ru.cristalix.core.scoreboard.IScoreboardService;
import ru.func.museum.data.ExcavationType;
import ru.func.museum.excavation.generator.ExcavationGenerator;
import ru.func.museum.player.User;
import ru.func.museum.util.MessageUtil;

public interface Excavation {

    World WORLD = Bukkit.getWorld("world");

    ExcavationGenerator getExcavationGenerator();
    String getTitle();
    double getCost();
    int getMinimalLevel();
    Location getStartLocation();
    int getBreakCount();

    default void load(User user) {
        ExcavationType excavation = user.getLastExcavation();
        Player player = user.getPlayer();
        player.getInventory().clear();
        player.getInventory().addItem(user.getPickaxeType().getItem());
        player.teleport(excavation.getStartLocation());

        IScoreboardService.get().setCurrentObjective(user.getUuid(), "excavation");

        player.sendTitle("§6Прибытие!", getTitle());

        MessageUtil.find("visitexcavation")
                .set("title", getTitle())
                .send(user);
        excavation.getExcavationGenerator().generateAndShow(player);
    }
}
