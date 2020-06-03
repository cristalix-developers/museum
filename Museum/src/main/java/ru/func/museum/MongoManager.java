package ru.func.museum;

import com.google.common.collect.ImmutableList;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import lombok.val;
import org.bson.Document;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.pojo.ClassModel;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.bukkit.Bukkit;
import ru.func.museum.element.Element;
import ru.func.museum.excavation.ExcavationType;
import ru.func.museum.museum.AbstractMuseum;
import ru.func.museum.museum.Museum;
import ru.func.museum.museum.collector.CollectorType;
import ru.func.museum.museum.space.SkeletonSpaceViewer;
import ru.func.museum.museum.space.Space;
import ru.func.museum.museum.template.MuseumTemplateType;
import ru.func.museum.player.Archaeologist;
import ru.func.museum.player.PlayerData;
import ru.func.museum.player.pickaxe.PickaxeType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.mongodb.client.model.Filters.eq;
import static org.bson.codecs.pojo.Conventions.ANNOTATION_CONVENTION;
import static org.bson.codecs.pojo.Conventions.CLASS_AND_PROPERTY_CONVENTION;

/**
 * @author func 23.05.2020
 * @project Museum
 */
public class MongoManager {
    private static MongoCollection<Archaeologist> mongoCollection;

    public static void connect(String uri, String database, String collection) {
        val codecProvider = PojoCodecProvider.builder()
                .conventions(ImmutableList.of(CLASS_AND_PROPERTY_CONVENTION, ANNOTATION_CONVENTION))
                .register(
                        ClassModel.builder(Archaeologist.class).enableDiscriminator(true).build(),
                        ClassModel.builder(PlayerData.class).enableDiscriminator(true).build(),
                        ClassModel.builder(AbstractMuseum.class).enableDiscriminator(true).build(),
                        ClassModel.builder(Space.class).enableDiscriminator(true).build(),
                        ClassModel.builder(SkeletonSpaceViewer.class).enableDiscriminator(true).build(),
                        ClassModel.builder(Museum.class).enableDiscriminator(true).build()
                ).automatic(true)
                .build();
        val codecRegistry = CodecRegistries.fromRegistries(
                MongoClientSettings.getDefaultCodecRegistry(),
                CodecRegistries.fromProviders(codecProvider)
        );
        mongoCollection = new MongoClient(new MongoClientURI(uri))
                .getDatabase(database)
                .withCodecRegistry(codecRegistry)
                .getCollection(collection, Archaeologist.class);
        Bukkit.getConsoleSender().sendMessage("§aConnected to database successfully.");
    }

    public static Archaeologist load(String name, String uuid) {
        Archaeologist found = mongoCollection.find(eq("uuid", uuid)).first();
        if (found == null) {
            List<Space> spaces = MuseumTemplateType.DEFAULT.getMuseumTemplate().getMatrix().get();
            found = PlayerData.builder()
                    .level(1)
                    .name(name)
                    .uuid(uuid)
                    .money(1000)
                    .exp(0)
                    .currentMuseum(0)
                    .lastExcavation(ExcavationType.DIRT)
                    .onExcavation(true)
                    .pickaxeType(PickaxeType.DEFAULT)
                    .elementList(Arrays.asList(
                            new Element(0, 0, spaces.get(0)),
                            new Element(0, 1, spaces.get(0)),
                            new Element(0, 2, spaces.get(0)),
                            new Element(0, 3, spaces.get(0)),
                            new Element(0, 4, spaces.get(0)),
                            new Element(0, 0, spaces.get(1)),
                            new Element(0, 1, spaces.get(1)),
                            new Element(0, 2, spaces.get(1)),
                            new Element(0, 3, spaces.get(1)),
                            new Element(0, 4, spaces.get(1))
                    )).museumList(Collections.singletonList(new Museum(
                            spaces,
                            "Музей в честь " + name,
                            MuseumTemplateType.DEFAULT,
                            CollectorType.DEFAULT
                    ))).friendList(new ArrayList<>())
                    .build();
            mongoCollection.insertOne(found);
        }
        Bukkit.getConsoleSender().sendMessage("§aLogged: " + found.toString());
        return found;
    }

    public static Archaeologist save(Archaeologist archaeologist) {
        //archaeologist.setOnExcavation(false);
        mongoCollection.updateOne(eq("uuid", archaeologist.getUuid()), new Document("$set", archaeologist));
        Bukkit.getConsoleSender().sendMessage("§aSaved: " + archaeologist.toString());
        return archaeologist;
    }
}
