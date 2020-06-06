package ru.func.museum.museum.space;

import org.bson.codecs.pojo.annotations.BsonDiscriminator;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import ru.func.museum.element.Element;
import ru.func.museum.player.Archaeologist;

import java.util.List;

@BsonDiscriminator
public interface Space {

    boolean isManipulator(Location location);

    void show(Archaeologist owner, Player guest);

    void hide(Archaeologist owner, Player guest);

    List<Element> getElements();
}
