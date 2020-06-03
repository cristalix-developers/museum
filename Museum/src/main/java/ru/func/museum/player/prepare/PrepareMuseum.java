package ru.func.museum.player.prepare;

import org.bukkit.entity.Player;
import ru.func.museum.App;
import ru.func.museum.museum.AbstractMuseum;
import ru.func.museum.player.Archaeologist;

/**
 * @author func 03.06.2020
 * @project Museum
 */
public class PrepareMuseum implements Prepare {
    @Override
    public void execute(Player player, Archaeologist archaeologist, App app) {
        AbstractMuseum museum = archaeologist.getMuseumList().get(archaeologist.getCurrentMuseum());
        museum.show(app, archaeologist, player);
        player.teleport(museum.getMuseumTemplateType().getMuseumTemplate().getSpawn());
    }
}
