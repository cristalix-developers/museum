package ru.func.museum.museum.hall.template.space;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import ru.func.museum.element.Element;
import ru.func.museum.player.User;

import java.util.List;

/**
 * @author func 22.05.2020
 * @project Museum
 */
@Getter
@AllArgsConstructor
public class SimpleSpaceViewer implements Space {

    @Override
    public boolean isManipulator(Location location) {
        return false;
    }

    @Override
    public void show(User owner, Player quest) {

    }

    @Override
    public void hide(User owner, Player guest) {

    }

    @Override
    public List<Element> getElements() {
        return null;
    }

    @Override
    public List<Integer> getAccessEntities() {
        return null;
    }
}
