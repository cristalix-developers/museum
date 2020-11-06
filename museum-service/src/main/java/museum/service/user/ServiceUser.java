package museum.service.user;

import lombok.Data;
import lombok.ToString;
import museum.data.UserInfo;
import museum.packages.UserInfoPackage;
import museum.realm.Realm;

import java.util.UUID;

import static museum.packages.UserInfoPackage.Action.DATA_REQUEST;

@Data
@ToString(onlyExplicitlyIncluded = true)
public class ServiceUser {

	@ToString.Include
	private final UUID uuid;

	@ToString.Include
	private Realm realm;

	private Realm tranferRealm;
	private long transferTime;

	private UserInfo info;

	public void disconnect(String message) {

	}

	public void syncData() {
		realm.send(new UserInfoPackage(uuid, null, DATA_REQUEST));
	}

	public String getName() {
		return info == null || info.getName() == null ? uuid.toString() : info.getName();
	}



}
