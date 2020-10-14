package museum;

import com.mongodb.async.client.MongoClient;
import museum.data.UserInfo;
import museum.packages.TopPackage;
import museum.tops.PlayerTopEntry;
import museum.tops.TopEntry;
import museum.utils.UtilCristalix;
import ru.cristalix.core.network.ISocketClient;
import ru.cristalix.core.network.packages.BulkGroupsPackage;
import ru.cristalix.core.network.packages.GroupData;
import ru.cristalix.core.network.packages.GroupsPackage;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class UserDataMongoAdapter extends MongoAdapter<UserInfo> {

    public UserDataMongoAdapter(MongoClient client, String dbName) {
        super(client, dbName, "userData", UserInfo.class);
    }

    public CompletableFuture<List<PlayerTopEntry<Object>>> getTop(TopPackage.TopType topType, int limit) {
        return makeRatingByField(topType.name().toLowerCase(), limit).thenApplyAsync(entries -> {
            List<PlayerTopEntry<Object>> playerEntries = entries
                    .stream()
                    .map(entry ->
                            new PlayerTopEntry<>(entry.getKey(), entry.getValue())
                    )
                    .collect(Collectors.toList());
            try {
                List<UUID> uuids = entries
                        .stream()
                        .map(entry ->
                                entry.getKey().getUuid()
                        )
                        .collect(Collectors.toList());
                List<GroupData> groups = ISocketClient.get()
                        .<BulkGroupsPackage>writeAndAwaitResponse(
                                new BulkGroupsPackage(uuids)
                        ).get(5L, TimeUnit.SECONDS)
                        .getGroups();
                for (int i = 0, j = playerEntries.size(); i < j; i++) {
                    GroupData data = groups.get(i);
                    PlayerTopEntry<Object> playerEntry = playerEntries.get(i);
                    playerEntry.setUserName(data.getUsername());
                    playerEntry.setDisplayName(UtilCristalix.createDisplayName(data));
                }
            } catch(Exception ex) {
                ex.printStackTrace();
                // Oh shit
                playerEntries.forEach(entry -> {
                    entry.setUserName("ERROR");
                    entry.setDisplayName("ERROR");
                });
            }
            return playerEntries;
        });
    }

}
