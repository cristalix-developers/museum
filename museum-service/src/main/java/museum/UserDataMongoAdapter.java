package museum;

import com.mongodb.async.client.MongoClient;
import museum.data.UserInfo;

public class UserDataMongoAdapter extends MongoAdapter<UserInfo> {

	public UserDataMongoAdapter(MongoClient client, String dbName) {
		super(client, dbName, "userData", UserInfo.class);
	}



}
