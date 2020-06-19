package ru.cristalix.museum;

import com.mongodb.async.client.MongoClients;
import com.mongodb.async.client.MongoCollection;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import ru.cristalix.core.GlobalSerializers;
import ru.cristalix.museum.data.UserInfo;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

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
