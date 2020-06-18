package ru.func.museum;

import com.google.common.collect.ImmutableList;
import com.mongodb.MongoClientSettings;
import com.mongodb.async.SingleResultCallback;
import com.mongodb.async.client.MongoClients;
import com.mongodb.async.client.MongoCollection;
import com.mongodb.client.model.Filters;
import lombok.val;
import org.bson.Document;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.pojo.ClassModel;
import org.bson.codecs.pojo.Conventions;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.bukkit.Bukkit;
import ru.cristalix.core.GlobalSerializers;
import ru.func.museum.data.UserInfo;
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
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static com.mongodb.client.model.Filters.eq;
import static org.bson.codecs.pojo.Conventions.ANNOTATION_CONVENTION;
import static org.bson.codecs.pojo.Conventions.CLASS_AND_PROPERTY_CONVENTION;

/**
 * @author func 23.05.2020
 * @project Museum
 */
public class MongoManager {
    private static MongoCollection<Document> mongoCollection;


    public static void connect(String uri, String database, String collection) {
        mongoCollection = MongoClients.create(uri)
                .getDatabase(database)
                .getCollection(collection);
    }

    public static CompletableFuture<String> load(UUID uuid) {
        CompletableFuture<String> archaeologist = new CompletableFuture<>();

        mongoCollection.find(Filters.eq("uuid", uuid.toString()))
                .first((result, t) -> archaeologist.complete(result.getString("data")));
        return archaeologist;
    }

    public static void save(UserInfo user, boolean insert) {
        String data = GlobalSerializers.toJson(user);
        String uuid = user.getUuid().toString();
        Document document = new Document("uuid", uuid);
        document.put("data", data);
        if (insert) {
            mongoCollection.insertOne(document, ((result, t) -> {
                if (t != null)
                    t.printStackTrace();
            }));
        } else {
            mongoCollection.updateOne(
                    Filters.eq("uuid", uuid),
                    new Document("$set", data),
                    (result, t) -> {
                        if (t != null)
                            t.printStackTrace();
                    }
            );
        }
    }
}
