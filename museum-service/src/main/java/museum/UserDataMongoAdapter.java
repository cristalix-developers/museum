package museum;

import com.mongodb.async.client.MongoClient;
import museum.data.UserInfo;
import museum.packages.TopPackage;
import museum.tops.TopEntry;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class UserDataMongoAdapter extends MongoAdapter<UserInfo> {

    public UserDataMongoAdapter(MongoClient client, String dbName) {
        super(client, dbName, "userData", UserInfo.class);
    }

    public CompletableFuture<List<TopEntry<UserInfo, Object>>> getTop(TopPackage.TopType topType, int limit) {
        return makeRatingByField(topType.name().toLowerCase(), limit);
    }

}
