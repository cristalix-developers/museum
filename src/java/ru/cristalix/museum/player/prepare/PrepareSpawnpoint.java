package ru.cristalix.museum.player.prepare;

import ru.cristalix.museum.App;
import ru.cristalix.museum.player.User;

/**
 * @author func 20.06.2020
 * @project museum
 */
public class PrepareSpawnpoint implements Prepare {
    @Override
    public void execute(User user, App app) {
        user.getPlayer().teleport(user.getCurrentMuseum().getPrototype().getSpawnPoint());
    }
}
