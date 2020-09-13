package museum;

import com.mongodb.async.client.MongoClient;
import museum.data.UserInfo;
import museum.tops.TopEntry;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class UserDataMongoAdapter extends MongoAdapter<UserInfo> {

    public UserDataMongoAdapter(MongoClient client, String dbName) {
        super(client, dbName, "userData", UserInfo.class);
    }

    public CompletableFuture<List<TopEntry<UserInfo, Double>>> getMoneyTop(int limit) {
        return makeRatingByField("money", limit);
    }
    
    public CompletableFuture<List<TopEntry<UserInfo, Long>>> getExperienceTop(int limit) {
        return makeRatingByField("experience", limit);
    }

}
