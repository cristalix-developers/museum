package ru.cristalix.museum;

import com.mongodb.async.client.MongoClient;
import com.mongodb.async.client.MongoClients;
import com.mongodb.async.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.UpdateOneModel;
import com.mongodb.client.model.UpdateOptions;
import org.bson.Document;
import ru.cristalix.core.GlobalSerializers;
import ru.cristalix.museum.data.UserInfo;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * @author func 23.05.2020
 * @project Museum
 */
public class MongoManager {

	private static MongoCollection<Document> userData;
	private static MongoCollection<Document> globalBoosters;

	public static void connect(String uri, String database, String collection) {
        MongoClient client = MongoClients.create(uri);
        userData = client.getDatabase(database).getCollection(collection);
//        globalBoosters = client.getDatabase(database).getCollection(???); ToDo: Global boosters to Mongo



	}

	public static CompletableFuture<UserInfo> load(UUID uuid) {
		CompletableFuture<String> archaeologist = new CompletableFuture<>();

		userData
				.find(Filters.eq("uuid", uuid.toString()))
				.first((result, throwable) -> archaeologist.complete(result == null ? null : result.getString("data")));
		return archaeologist.thenApply(data -> data == null ? null : GlobalSerializers.fromJson(data, UserInfo.class));
	}

	public static void save(UserInfo user) {
		String uuid = user.getUuid().toString();
		userData.updateOne(
				Filters.eq("uuid", uuid),
				new Document("$set", new Document("data", GlobalSerializers.toJson(user))),
				new UpdateOptions().upsert(true),
				(result, throwable) -> {
                    Optional.ofNullable(throwable).ifPresent(Throwable::printStackTrace);
				}
		);
	}

	public static void bulkSave(List<UserInfo> users) {
		List<UpdateOneModel<Document>> models = users.stream().map(info -> new UpdateOneModel<Document>(
				Filters.eq("uuid", info.getUuid().toString()),
				new Document("$set", new Document("data", GlobalSerializers.toJson(info))),
				new UpdateOptions().upsert(true)
		)).collect(Collectors.toList());
		userData.bulkWrite(models, (result, throwable) -> {
			if (throwable != null)
				throwable.printStackTrace();
		});
	}

}
