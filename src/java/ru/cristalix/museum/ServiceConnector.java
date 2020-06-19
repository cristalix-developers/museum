package ru.cristalix.museum;

import ru.cristalix.core.CoreApi;
import ru.cristalix.core.network.ISocketClient;
import ru.cristalix.core.network.packages.PacketForwardPackage;
import ru.cristalix.core.realm.RealmId;
import ru.cristalix.museum.data.UserInfo;
import ru.cristalix.museum.packages.UserInfoPackage;

import java.util.UUID;
import java.util.concurrent.ExecutionException;

public class ServiceConnector {

	private final ISocketClient client;
	private final RealmId serviceRealm;

	public ServiceConnector(App app) {
		this.client = CoreApi.get().getSocketClient();
		this.serviceRealm = RealmId.of(app.getConfig().getString("museum_service"));
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
			// todo сохранение не реализованно до конца
			UserInfoPackage packet = (UserInfoPackage) client.awaitResponse(new PacketForwardPackage(serviceRealm, new UserInfoPackage(uuid, info))).get();
			return packet.getUserInfo();
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
			return null;
		}
	}

}
