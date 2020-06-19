package ru.cristalix.museum.player.prepare;

import org.bukkit.Bukkit;
import ru.cristalix.museum.App;
import ru.cristalix.museum.player.User;

/**
 * @author func 11.06.2020
 * @project Museum
 */
public class PreparePlayers implements Prepare {

    @Override
    public void execute(User user, App app) {
        Bukkit.getOnlinePlayers().forEach(current -> user.getPlayer().hidePlayer(app, current));

    }
}
