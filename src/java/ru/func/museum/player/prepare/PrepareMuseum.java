package ru.func.museum.player.prepare;

import org.bukkit.entity.Player;
import ru.func.museum.App;
import ru.func.museum.player.User;

/**
 * @author func 03.06.2020
 * @project Museum
 */
public class PrepareMuseum implements Prepare {
    @Override
    public void execute(User user, App app) {
        user.getMuseums().get(0).load(app, user);
    }
}
