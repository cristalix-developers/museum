package ru.func.museum.player.prepare;

import org.bukkit.entity.Player;
import ru.func.museum.App;
import ru.func.museum.player.Archaeologist;

import java.lang.invoke.CallSite;

/**
 * @author func 03.06.2020
 * @project Museum
 */
public class PrepareMuseum implements Prepare {
    @Override
    public void execute(Player player, Archaeologist archaeologist, App app) {
        archaeologist.getMuseumList().get(0).load(app, archaeologist, player);
    }
}
