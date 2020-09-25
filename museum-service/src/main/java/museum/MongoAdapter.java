package museum;

import com.mongodb.ClientSessionOptions;
import com.mongodb.async.client.FindIterable;
import com.mongodb.async.client.MongoClient;
import com.mongodb.async.client.MongoCollection;
import com.mongodb.client.model.*;
import com.mongodb.session.ClientSession;
import lombok.Getter;
import lombok.val;
import museum.data.Unique;
import museum.tops.TopEntry;
import org.bson.Document;
import ru.cristalix.core.GlobalSerializers;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

@Getter
public class MongoAdapter<T extends Unique> {

	private static final UpdateOptions UPSERT = new UpdateOptions().upsert(true);

	private final MongoCollection<Document> data;
	private final Class<T> type;
	private final ClientSession session;

	private final AtomicBoolean connected = new AtomicBoolean(false);

	public MongoAdapter(MongoClient client, String database, String collection, Class<T> type) {
		this.data = client.getDatabase(database).getCollection(collection);
		this.type = type;
		CompletableFuture<ClientSession> future = new CompletableFuture<>();
		client.startSession(ClientSessionOptions.builder().causallyConsistent(true).build(), (s, throwable) -> {
			if (throwable != null) future.completeExceptionally(throwable);
			else future.complete(s);
		});
		try {
			this.session = future.get(10, TimeUnit.SECONDS);
			new Thread(() -> {
				try {
					Thread.sleep(2000L);
					connected.set(true);
				} catch(Exception ignored) {}
			}).start();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public boolean isConnected() {
		return connected.get();
	}

	public CompletableFuture<T> find(UUID uuid) {
		CompletableFuture<T> future = new CompletableFuture<>();

		data.find(session, Filters.eq("uuid", uuid.toString()))
				.first((result, throwable) -> future.complete(readDocument(result)));
		return future;
	}

	public CompletableFuture<Map<UUID, T>> findAll() {
		CompletableFuture<Map<UUID, T>> future = new CompletableFuture<>();
		FindIterable<Document> documentFindIterable = data.find(session);
		Map<UUID, T> map = new ConcurrentHashMap<>();
		documentFindIterable.forEach(document -> {
			T object = readDocument(document);
			map.put(object.getUuid(), object);
		}, (v, error) -> future.complete(map));
		return future;
	}

	private T readDocument(Document document) {
		return document == null ? null : GlobalSerializers.fromJson(document.toJson(), type);
	}

	public void save(Unique unique) {
		save(Collections.singletonList(unique));
	}

	public void save(List<Unique> uniques) {
		List<WriteModel<Document>> models = new ArrayList<>();
		for (Unique unique : uniques) {
			WriteModel<Document> model = new UpdateOneModel<>(
					Filters.eq("uuid", unique.getUuid().toString()),
					new Document("$set", Document.parse(GlobalSerializers.toJson(unique))),
					UPSERT
			);
			models.add(model);
		}

		data.bulkWrite(session, models, this::handle);
	}

	public <V> CompletableFuture<List<TopEntry<T, V>>> makeRatingByField(String fieldName, int limit) {
		val operations = Arrays.asList(
				Aggregates.project(Projections.fields(Projections.include("uuid", fieldName), Projections.exclude("_id"))),
				Aggregates.sort(Sorts.descending(fieldName)),
				Aggregates.limit(limit)
		);
		List<TopEntry<T, V>> entries = new ArrayList<>();
		CompletableFuture<List<TopEntry<T, V>>> future = new CompletableFuture<>();
		data.aggregate(operations).forEach(document -> {
			T key = readDocument(document);
			entries.add(new TopEntry<>(key, (V) document.get(fieldName)));
		}, (__, throwable) -> {
			if (throwable != null) {
				future.completeExceptionally(throwable);
				return;
			}
			future.complete(entries);
		});
		return future;
	}

	private void handle(Object result, Throwable throwable) {
		if (throwable != null)
			throwable.printStackTrace();
	}

	public void clear(UUID uuid) {
		data.deleteOne(Filters.eq("uuid", uuid.toString()), this::handle);
	}

}
