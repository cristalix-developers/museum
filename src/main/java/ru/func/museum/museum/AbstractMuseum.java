package ru.func.museum.museum;

import org.bson.codecs.pojo.annotations.BsonDiscriminator;
import ru.func.museum.museum.collector.CollectorType;
import ru.func.museum.museum.space.Space;
import ru.func.museum.museum.template.MuseumTemplateType;

import java.util.List;

@BsonDiscriminator
public interface AbstractMuseum {

    List<Space> getMatrix();

    String getTitle();

    void setTitle(String title);

    MuseumTemplateType getMuseumTemplateType();

    CollectorType getCollectorType();

    void setCollectorType(CollectorType collectorType);
}
