package ru.func.museum.museum.space;

import org.bson.codecs.pojo.annotations.BsonDiscriminator;
import org.bukkit.entity.Player;
import ru.func.museum.player.Archaeologist;

@BsonDiscriminator
public interface Space {

   // Location getManipulator();

    void show(Archaeologist owner, Player guest);

    void hide(Archaeologist owner, Player guest);
}
