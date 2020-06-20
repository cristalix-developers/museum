package ru.cristalix.museum;

import com.mongodb.async.client.MongoClients;
import com.mongodb.async.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.UpdateOneModel;
import com.mongodb.client.model.UpdateOptions;
import org.bson.Document;
import ru.cristalix.core.GlobalSerializers;
import ru.cristalix.museum.data.UserInfo;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

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

        mongoCollection
                .find(Filters.eq("uuid", uuid.toString()))
                .first((result, t) -> archaeologist.complete(result == null ? null : result.getString("data")));
        return archaeologist;
    }

    public static void save(UserInfo user) {
        String uuid = user.getUuid().toString();
        mongoCollection.updateOne(
                Filters.eq("uuid", uuid),
                new Document("$set", new Document("data", GlobalSerializers.toJson(user))),
                new UpdateOptions().upsert(true),
                (result, t) -> {
                    if (t != null)
                        t.printStackTrace();
                }
        );
    }

    public static void bulkSave(List<UserInfo> users) {
        List<UpdateOneModel<Document>> models = users.stream().map(
                info -> new UpdateOneModel<Document>(
                        Filters.eq("uuid", info.getUuid().toString()),
                        new Document("$set", GlobalSerializers.toJson(info)),
                        new UpdateOptions().upsert(true)
                )
        ).collect(Collectors.toList());
        mongoCollection.bulkWrite(models, (result, t) -> {
            if (t != null)
                t.printStackTrace();
        });
    }
}
