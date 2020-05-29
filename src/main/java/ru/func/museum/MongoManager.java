package ru.func.museum;

import com.google.common.collect.ImmutableList;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import org.bson.Document;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.ClassModel;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import ru.func.museum.element.Element;
import ru.func.museum.element.ElementType;
import ru.func.museum.excavation.ExcavationType;
import ru.func.museum.museum.AbstractMuseum;
import ru.func.museum.museum.Museum;
import ru.func.museum.museum.collector.CollectorType;
import ru.func.museum.museum.template.MuseumTemplateType;
import ru.func.museum.player.Archaeologist;
import ru.func.museum.player.PlayerData;
import ru.func.museum.player.pickaxe.PickaxeType;

import java.util.ArrayList;
import java.util.Collections;

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
        PojoCodecProvider codecProvider = PojoCodecProvider.builder()
                .conventions(ImmutableList.of(CLASS_AND_PROPERTY_CONVENTION, ANNOTATION_CONVENTION))
                .register(
                        ClassModel.builder(Archaeologist.class).enableDiscriminator(true).build(),
                        ClassModel.builder(PlayerData.class).enableDiscriminator(true).build(),
                        ClassModel.builder(AbstractMuseum.class).enableDiscriminator(true).build(),
                        ClassModel.builder(Museum.class).enableDiscriminator(true).build()
                ).automatic(true)
                .build();
        CodecRegistry codecRegistry = CodecRegistries.fromRegistries(
                MongoClientSettings.getDefaultCodecRegistry(),
                CodecRegistries.fromProviders(codecProvider)
        );
        mongoCollection = new MongoClient(new MongoClientURI(uri))
                .getDatabase(database)
                .withCodecRegistry(codecRegistry)
                .getCollection(collection, Archaeologist.class);
        Bukkit.getConsoleSender().sendMessage("§aConnected to database successfully.");
    }

    public static Archaeologist load(Player player) {
        Archaeologist found = mongoCollection.find(eq("uuid", player.getUniqueId().toString())).first();
        if (found == null) {
            found = PlayerData.builder()
                    .level(1)
                    .name(player.getName())
                    .uuid(player.getUniqueId().toString())
                    .money(1000)
                    .lastExcavation(ExcavationType.NOOP)
                    .onExcavation(false)
                    .pickaxeType(PickaxeType.DEFAULT)
                    .museumList(Collections.singletonList(new Museum(
                            MuseumTemplateType.DEFAULT.getMuseumTemplate().getMatrix().get(),
                            "Музей в честь " + player.getName(),
                            MuseumTemplateType.DEFAULT,
                            CollectorType.DEFAULT
                    ))).elementList(Collections.singletonList(new Element(
                            ElementType.BONE_DINOSAUR_LEG_LEFT,
                            null,
                            1
                    ))).friendList(new ArrayList<>())
                    .build();
            mongoCollection.insertOne(found);
        }
        Bukkit.getConsoleSender().sendMessage("§aLogged: " + found.toString());
        return found;
    }

    public static Archaeologist save(Archaeologist archaeologist) {
        mongoCollection.updateOne(eq("uuid", archaeologist.getUuid()), new Document("$set", archaeologist));
        Bukkit.getConsoleSender().sendMessage("§aSaved: " + archaeologist.toString());
        return archaeologist;
    }
}
