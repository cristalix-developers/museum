package ru.func.museum.museum.space;

import org.bson.codecs.pojo.annotations.BsonDiscriminator;
import org.bukkit.entity.Player;
import ru.func.museum.player.Archaeologist;

@BsonDiscriminator
public interface Space {

    void show(Archaeologist owner, Player guest);
}
