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
import ru.func.museum.museum.Museum;
import ru.func.museum.museum.collector.CollectorType;
import ru.func.museum.museum.hall.Hall;
import ru.func.museum.museum.hall.template.space.SkeletonSubject;
import ru.func.museum.museum.hall.template.space.Subject;
import ru.func.museum.player.User;
import ru.func.museum.player.pickaxe.PickaxeType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.concurrent.CompletableFuture;

import static com.mongodb.client.model.Filters.eq;
import static org.bson.codecs.pojo.Conventions.ANNOTATION_CONVENTION;
import static org.bson.codecs.pojo.Conventions.CLASS_AND_PROPERTY_CONVENTION;

/**
 * @author func 23.05.2020
 * @project Museum
 */
public class MongoManager {
    private static MongoCollection<User> mongoCollection;

    public static void connect(String uri, String database, String collection) {
        val codecProvider = PojoCodecProvider.builder()
                .conventions(ImmutableList.of(CLASS_AND_PROPERTY_CONVENTION, ANNOTATION_CONVENTION))
                .register(
                        ClassModel.builder(User.class).enableDiscriminator(true).build(),
                        ClassModel.builder(User.class).enableDiscriminator(true).build(),
                        ClassModel.builder(Museum.class).enableDiscriminator(true).build(),
                        ClassModel.builder(Subject.class).enableDiscriminator(true).build(),
                        ClassModel.builder(SkeletonSubject.class).enableDiscriminator(true).build(),
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
                .getCollection(collection, User.class);
        Bukkit.getConsoleSender().sendMessage("§aConnected to database successfully.");
    }

    public static CompletableFuture<User> load(String name, String uuid) {
        CompletableFuture<User> archaeologist = new CompletableFuture<>();

        mongoCollection.find(eq("uuid", uuid)).first((result, t) -> {
            if (result == null) {
                result = User.builder()
                        .level(1)
                        .name(name)
                        .uuid(uuid)
                        .money(1_000_000)
                        .exp(0)
                        .excavationCount(0)
                        .breakLess(0)
                        .pickedCoinsCount(0)
                        .lastExcavation(ExcavationType.DIRT)
                        .onExcavation(false)
                        .pickaxeType(PickaxeType.DEFAULT)
                        .elementList(new ArrayList<>())
                        .museumList(Collections.singletonList(new Museum(
                                new Date(),
                                Collections.singletonList(new Hall(
                                        HallTemplateType.DEFAULT.getHallTemplate().getMatrix().get(),
                                        HallTemplateType.DEFAULT,
                                        CollectorType.PRESTIGE
                                )), "Музей в честь " + name,
                                -91, 90, 251
                        ))).build();
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

    public static void save(User archaeologist) {
        mongoCollection.updateOne(
                eq("uuid", archaeologist.getUuid()),
                new Document("$set", archaeologist),
                (result, t) -> Bukkit.getConsoleSender().sendMessage("§aSaved: " + archaeologist.toString())
        );
    }
}
