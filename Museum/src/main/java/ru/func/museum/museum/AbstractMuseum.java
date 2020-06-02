package ru.func.museum.museum;

import org.bson.codecs.pojo.annotations.BsonDiscriminator;
import org.bukkit.entity.Player;
import ru.func.museum.museum.collector.CollectorType;
import ru.func.museum.museum.space.Space;
import ru.func.museum.museum.template.MuseumTemplateType;
import ru.func.museum.player.Archaeologist;

import java.util.List;

@BsonDiscriminator
public interface AbstractMuseum {

    List<Space> getMatrix();

    void show(Archaeologist archaeologist, Player guest);

    String getTitle();

    void setTitle(String title);

    MuseumTemplateType getMuseumTemplateType();

    CollectorType getCollectorType();

    void setCollectorType(CollectorType collectorType);
}
