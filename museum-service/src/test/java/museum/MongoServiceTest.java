package museum;

import com.mongodb.async.SingleResultCallback;
import com.mongodb.async.client.MongoClient;
import com.mongodb.async.client.MongoClients;
import com.mongodb.client.model.Filters;
import com.mongodb.client.result.DeleteResult;
import museum.data.PickaxeType;
import museum.data.UserInfo;
import museum.packages.TopPackage;
import museum.service.data.MongoAdapter;
import museum.tops.TopEntry;
import org.awaitility.Awaitility;
import org.bson.Document;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import ru.cristalix.core.GlobalSerializers;
import ru.cristalix.core.math.V3;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.ToDoubleFunction;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertIterableEquals;

public class MongoServiceTest {

    private static final Random RANDOM = new Random();
    private static UserDataMongoAdapter userData;

    @BeforeAll
    static void init() {
        MongoClient client = MongoClients.create(System.getenv("db_url"));
        String dbName = System.getenv("db_data");
        userData = new UserDataMongoAdapter(client, dbName);
        Awaitility.await().atMost(5L, TimeUnit.SECONDS).until(userData::isConnected);
    }

    @Test
    void testMoneyTop() {
        testTop(TopPackage.TopType.MONEY, UserInfo::getMoney, 50, 50);
    }

    @Test
    void testMoneyTopLimit() {
        testTop(TopPackage.TopType.MONEY, UserInfo::getMoney, 50, 15);
    }

    @Test
    void testSalaryTop() {
        testTop(TopPackage.TopType.INCOME, UserInfo::getIncome, 50, 50);
    }

    @Test
    void testSalaryTopLimit() {
        testTop(TopPackage.TopType.INCOME, UserInfo::getIncome, 50, 15);
    }

    @Test
    void testExperienceTop() {
        testTop(TopPackage.TopType.EXPERIENCE, UserInfo::getExperience, 50, 50);
    }

    @Test
    void testExperienceTopLimit() {
        testTop(TopPackage.TopType.EXPERIENCE, UserInfo::getExperience, 50, 15);
    }

    void testTop(TopPackage.TopType topType, ToDoubleFunction<UserInfo> valueExtractor, int generate, int limit) {
//        ThreadUtils.sleep(100L, );
        MongoAdapter<UserInfo> adapter = userData;
        CompletableFuture<Void> dropFuture = new CompletableFuture<>();
        adapter.getData().drop(adapter.getSession(), wrap(dropFuture));
        dropFuture.join();
        List<UserInfo> generated = generateUserInfos(generate);
        pushAll(adapter, generated.stream().map(info -> Document.parse(GlobalSerializers.toJson(info))).collect(Collectors.toList())).join();
        List<TopEntry<UserInfo, Object>> list = adapter.aggregateTop(topType.name().toLowerCase(), limit).join();
        List<UUID> expected = generated.stream().sorted(Comparator.comparingDouble(valueExtractor).reversed()).map(UserInfo::getUuid).collect(Collectors.toList());
        deleteAll(adapter, expected).join();
        assertIterableEquals(
                expected.stream().limit(limit).collect(Collectors.toList()),
                list.stream().map(TopEntry::getKey).map(UserInfo::getUuid).collect(Collectors.toList())
        );
    }

    CompletableFuture<Void> pushAll(MongoAdapter<UserInfo> adapter, List<Document> documents) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        adapter.getData().insertMany(documents, wrap(future));
        return future;
    }

    CompletableFuture<DeleteResult> deleteAll(MongoAdapter<UserInfo> adapter, List<UUID> uuids) {
        CompletableFuture<DeleteResult> future = new CompletableFuture<>();
        adapter.getData().deleteMany(
                Filters.or(
                    uuids.stream().map(uid -> Filters.eq("uuid", uid.toString())).collect(Collectors.toList())
                ), wrap(future)
        );
        return future;
    }

    <T> SingleResultCallback<T> wrap(CompletableFuture<T> future) {
        return (obj, err) -> {
            if (err != null) {
                future.completeExceptionally(err);
                err.printStackTrace();
                return;
            }
            future.complete(obj);
        };
    }

    List<UserInfo> generateUserInfos(int count) {
        List<UserInfo> infos = new ArrayList<>(count);
        for (int i = 0; i < count; i++) infos.add(generateUserInfo());
        return infos;
    }

    UserInfo generateUserInfo() {
        return new UserInfo(UUID.randomUUID(),
                RANDOM.nextInt(1000000),
                RANDOM.nextInt(1000000),
                RANDOM.nextInt(1000000),
                PickaxeType.DEFAULT,
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                RANDOM.nextInt(100),
                RANDOM.nextInt(1000000),
                new V3(0, 0, 0),
                Collections.emptyList(),
                Collections.emptyList(),
                RANDOM.nextInt(1000000)
        );
    }
}