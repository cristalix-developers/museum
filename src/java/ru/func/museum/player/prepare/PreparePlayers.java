package ru.func.museum.player.prepare;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import ru.func.museum.App;
import ru.func.museum.player.Archaeologist;

/**
 * @author func 11.06.2020
 * @project Museum
 */
public class PreparePlayers implements Prepare {
    @Override
    public void execute(Player player, Archaeologist archaeologist, App app) {
        Bukkit.getOnlinePlayers().forEach(current -> player.hidePlayer(app, current));
    }
}
