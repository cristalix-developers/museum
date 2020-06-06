package ru.func.museum;

import com.google.common.collect.ImmutableList;
import com.mongodb.MongoClientSettings;
import com.mongodb.async.client.MongoClients;
import com.mongodb.async.client.MongoCollection;
import lombok.val;
import org.bson.Document;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.pojo.ClassModel;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.bukkit.Bukkit;
import ru.func.museum.excavation.ExcavationType;
import ru.func.museum.museum.AbstractMuseum;
import ru.func.museum.museum.CollectorType;
import ru.func.museum.museum.Museum;
import ru.func.museum.museum.space.SkeletonSpaceViewer;
import ru.func.museum.museum.space.Space;
import ru.func.museum.museum.template.MuseumTemplateType;
import ru.func.museum.player.Archaeologist;
import ru.func.museum.player.PlayerData;
import ru.func.museum.player.pickaxe.PickaxeType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;

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
        mongoCollection = MongoClients.create(uri)
                .getDatabase(database)
                .withCodecRegistry(codecRegistry)
                .getCollection(collection, Archaeologist.class);
        Bukkit.getConsoleSender().sendMessage("§aConnected to database successfully.");
    }

    public static CompletableFuture<Archaeologist> load(String name, String uuid) {
        CompletableFuture<Archaeologist> archaeologist = new CompletableFuture<>();
        List<Space> spaces = MuseumTemplateType.DEFAULT.getMuseumTemplate().getMatrix().get();

        mongoCollection.find(eq("uuid", uuid)).first((result, t) -> {
            if (result == null) {
                AbstractMuseum museum = new Museum(
                        new Date(),
                        spaces,
                        "Музей в честь " + name,
                        MuseumTemplateType.DEFAULT,
                        CollectorType.NONE
                );
                result = PlayerData.builder()
                        .level(1)
                        .name(name)
                        .uuid(uuid)
                        .money(1_000_000)
                        .exp(0)
                        .excavationCount(0)
                        .breakLess(0)
                        .lastExcavation(ExcavationType.DIRT)
                        .onExcavation(false)
                        .pickaxeType(PickaxeType.DEFAULT)
                        .currentMuseum(museum)
                        .elementList(new ArrayList<>())
                        .friendList(new ArrayList<>())
                        .museumList(Collections.singletonList(museum))
                        .build();
                Bukkit.getConsoleSender().sendMessage("§aLogged: " + result);

                archaeologist.complete(result);

                mongoCollection.insertOne(
                        result,
                        (resultVoid, th) -> Bukkit.getConsoleSender().sendMessage("§aLogged: " + resultVoid)
                );
            } else
                archaeologist.complete(result);
        });
        return archaeologist;
    }

    public static void save(Archaeologist archaeologist) {
        mongoCollection.updateOne(
                eq("uuid", archaeologist.getUuid()),
                new Document("$set", archaeologist),
                (result, t) -> Bukkit.getConsoleSender().sendMessage("§aSaved: " + archaeologist.toString())
        );
    }
}
