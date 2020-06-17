package ru.func.museum.player.prepare;

import org.bukkit.entity.Player;
import ru.func.museum.App;
import ru.func.museum.player.Archaeologist;

public interface Prepare {
    void execute(Player player, Archaeologist archaeologist, App app);
}
