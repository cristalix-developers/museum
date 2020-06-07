package ru.func.museum.museum;

import org.bson.codecs.pojo.annotations.BsonDiscriminator;
import org.bukkit.entity.Player;
import ru.func.museum.App;
import ru.func.museum.museum.hall.Hall;
import ru.func.museum.player.Archaeologist;

import java.util.Date;
import java.util.List;

@BsonDiscriminator
public interface AbstractMuseum {

    Date getDate();

    long getViews();

    Archaeologist getOwner();

    List<Hall> getHalls();

    void load(App plugin, Archaeologist archaeologist, Player guest);

    void unload(Archaeologist archaeologist, Player guest);

    void updateIncrease();

    double getSummaryIncrease();

    String getTitle();

    void setTitle(String title);
}
