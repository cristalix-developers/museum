package ru.func.museum.museum;

import org.bson.codecs.pojo.annotations.BsonDiscriminator;
import org.bukkit.entity.Player;
import ru.func.museum.App;
import ru.func.museum.museum.space.Space;
import ru.func.museum.museum.template.MuseumTemplateType;
import ru.func.museum.player.Archaeologist;

import java.util.Date;
import java.util.List;

@BsonDiscriminator
public interface AbstractMuseum {

    Date getDate();

    long getViews();

    Archaeologist getOwner();

    List<Space> getMatrix();

    void load(App plugin, Archaeologist archaeologist, Player guest);

    void unload(App plugin, Archaeologist archaeologist, Player guest);

    void updateIncrease();

    double getSummaryIncrease();

    String getTitle();

    void setTitle(String title);

    MuseumTemplateType getMuseumTemplateType();

    CollectorType getCollectorType();

    void setCollectorType(CollectorType collectorType);
}
