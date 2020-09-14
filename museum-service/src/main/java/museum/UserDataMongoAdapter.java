package museum;

import com.mongodb.async.client.MongoClient;
import museum.data.UserInfo;
import museum.packages.TopPackage;
import museum.tops.TopEntry;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class UserDataMongoAdapter extends MongoAdapter<UserInfo> {

    public UserDataMongoAdapter(MongoClient client, String dbName) {
        super(client, dbName, "userData", UserInfo.class);
    }

    public CompletableFuture<List<TopEntry<UserInfo, Object>>> getTop(TopPackage.TopType topType, int limit) {
        switch (topType) {
            case MONEY:
                return getMoneyTop(limit);
            case SALARY:
                return getSalaryTop(limit);
            case EXPERIENCE:
                return getExperienceTop(limit);
            default:
                System.out.println("We don't know type of this top: " + topType);
                return CompletableFuture.completedFuture(Collections.emptyList());
        }
    }

    public CompletableFuture<List<TopEntry<UserInfo, Object>>> getSalaryTop(int limit) {
        return makeRatingByField("income", limit);
    }

    public CompletableFuture<List<TopEntry<UserInfo, Object>>> getMoneyTop(int limit) {
        return makeRatingByField("money", limit);
    }
    
    public CompletableFuture<List<TopEntry<UserInfo, Object>>> getExperienceTop(int limit) {
        return makeRatingByField("experience", limit);
    }

}
