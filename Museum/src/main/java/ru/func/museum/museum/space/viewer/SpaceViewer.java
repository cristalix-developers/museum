package ru.func.museum.museum.space.viewer;

import org.bukkit.entity.Player;
import ru.func.museum.player.Archaeologist;

public interface SpaceViewer {

    void show(Archaeologist owner, Player quest);
}
