package ru.func.museum.museum.hall.template.space;

import org.bukkit.Location;
import ru.func.museum.element.Element;
import ru.func.museum.player.User;

import java.util.List;

public interface Space {

    void show(User owner);

    void hide(User owner);

    List<Element> getElements();

    List<Integer> getAccessEntities();

    double getIncome();

}
