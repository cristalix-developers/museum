package museum;

import static org.junit.jupiter.api.Assertions.*;

import com.mongodb.async.client.MongoClient;
import com.mongodb.async.client.MongoClients;
import com.mongodb.client.model.Filters;
import museum.data.PickaxeType;
import museum.data.UserInfo;
import museum.packages.TopPackage;
import museum.tops.TopEntry;
import org.awaitility.Awaitility;
import org.bson.Document;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import ru.cristalix.core.GlobalSerializers;
import ru.cristalix.core.math.V3;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class MongoServiceTest {

    private static final Random RANDOM = new Random();
    private static UserDataMongoAdapter userData;

    @BeforeAll
    static void init() throws Exception {
        MongoClient client = MongoClients.create(System.getenv("db_url"));
        String dbName = System.getenv("db_data");
        userData = new UserDataMongoAdapter(client, dbName);
        Awaitility.await().atMost(5L, TimeUnit.SECONDS).until(userData::isConnected);
    }

    @Test
    void testMoneyTop() throws Exception {
        testTop(TopPackage.TopType.MONEY, Comparator.comparingDouble(UserInfo::getMoney).reversed(), 50, 50);
    }

    @Test
    void testMoneyTopLimit() throws Exception {
        testTop(TopPackage.TopType.MONEY, Comparator.comparingDouble(UserInfo::getMoney).reversed(), 50, 15);
    }

    @Test
    void testSalaryTop() throws Exception {
        testTop(TopPackage.TopType.SALARY, Comparator.comparingDouble(UserInfo::getIncome).reversed(), 50, 50);
    }

    @Test
    void testSalaryTopLimit() throws Exception {
        testTop(TopPackage.TopType.SALARY, Comparator.comparingDouble(UserInfo::getIncome).reversed(), 50, 15);
    }

    @Test
    void testExperienceTop() throws Exception {
        testTop(TopPackage.TopType.EXPERIENCE, Comparator.comparingDouble(UserInfo::getExperience).reversed(), 50, 50);
    }

    @Test
    void testExperienceTopLimit() throws Exception {
        testTop(TopPackage.TopType.EXPERIENCE, Comparator.comparingDouble(UserInfo::getExperience).reversed(), 50, 15);
    }

    void testTop(TopPackage.TopType topType, Comparator<UserInfo> comparator, int generate, int limit) throws Exception {
        Thread.sleep(500L);
        UserDataMongoAdapter adapter = userData;
        adapter.getData().drop(adapter.getSession(), (v, err) -> {
            if (err != null)
                err.printStackTrace();
        });
        Thread.sleep(2000L);
        List<UserInfo> generated = generateUserInfos(generate);
        pushAll(adapter, generated.stream().map(info -> Document.parse(GlobalSerializers.toJson(info))).collect(Collectors.toList()));
        Thread.sleep(500L);
        List<TopEntry<UserInfo, Object>> list = adapter.getTop(topType, limit).join();
        List<UUID> expected = generated.stream().sorted(comparator).map(UserInfo::getUuid).collect(Collectors.toList());
        deleteAll(adapter, expected);
        Thread.sleep(300L);
        assertIterableEquals(
                expected.stream().limit(limit).collect(Collectors.toList()),
                list.stream().map(TopEntry::getKey).map(UserInfo::getUuid).collect(Collectors.toList())
        );
    }

    void pushAll(MongoAdapter adapter, List<Document> documents) {
        adapter.getData().insertMany(documents, (v, err) -> {
            if (err != null)
                err.printStackTrace();
        });
    }

    void deleteAll(MongoAdapter adapter, List<UUID> uuids) {
        adapter.getData().deleteMany(
                Filters.or(
                    uuids.stream().map(uid -> Filters.eq("uuid", uid.toString())).collect(Collectors.toList())
                ), (v, err) -> {
                    if (err != null)
                        err.printStackTrace();
                }
        );
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
                PickaxeType.DEFAULT, Collections.emptyList(), Collections.emptyList(), Collections.emptyList(),
                RANDOM.nextInt(100), RANDOM.nextInt(1000000), new V3(0, 0, 0), Collections.emptyList(), Collections.emptyList(),
                RANDOM.nextInt(1000000));
    }

}
