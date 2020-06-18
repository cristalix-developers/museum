package ru.func.museum;

import ru.cristalix.core.CoreApi;
import ru.cristalix.core.network.ISocketClient;
import ru.cristalix.core.network.packages.PacketForwardPackage;
import ru.cristalix.core.realm.RealmId;
import ru.func.museum.data.UserInfo;
import ru.func.museum.packages.UserInfoPackage;

import java.util.UUID;
import java.util.concurrent.ExecutionException;

public class ServiceConnector {

	private final App app;
	private final ISocketClient client;
	private final RealmId serviceRealm;

	public ServiceConnector(App app) {
		this.app = app;
		this.client = CoreApi.get().getSocketClient();
		this.serviceRealm = RealmId.of(System.getProperty("MUSEUM_SERVICE"));
	}

	public UserInfo loadUserSync(UUID uuid) {
		try {
			UserInfoPackage packet = (UserInfoPackage) client.awaitResponse(new PacketForwardPackage(serviceRealm, new UserInfoPackage(uuid))).get();
			return packet.getUserInfo();
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
			return null;
		}
	}


	public UserInfo saveUser(UUID uuid, UserInfo info) {
		try {
			UserInfoPackage packet = (UserInfoPackage) client.awaitResponse(new PacketForwardPackage(serviceRealm, new UserInfoPackage(uuid))).get();
			return packet.getUserInfo();
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
			return null;
		}
	}

}
