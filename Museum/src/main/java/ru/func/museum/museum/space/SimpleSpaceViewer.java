package ru.func.museum.museum.space;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.entity.Player;
import ru.func.museum.player.Archaeologist;

/**
 * @author func 22.05.2020
 * @project Museum
 */
@Getter
@AllArgsConstructor
public class SimpleSpaceViewer implements Space {

 /*   @Override
    public Location getManipulator() {
        return null;
    }*/

    @Override
    public void show(Archaeologist owner, Player quest) {

    }

    @Override
    public void hide(Archaeologist owner, Player guest) {

    }
}
