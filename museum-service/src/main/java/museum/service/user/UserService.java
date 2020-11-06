package museum.service.user;

import clepto.LoggerUtils;
import lombok.RequiredArgsConstructor;
import lombok.val;
import museum.MongoAdapter;
import museum.MuseumService;
import museum.data.UserInfo;
import museum.packages.*;
import museum.realm.Realm;
import museum.tops.PlayerTopEntry;
import museum.utils.UtilCristalix;
import ru.cristalix.core.network.ISocketClient;
import ru.cristalix.core.network.packages.BulkGroupsPackage;
import ru.cristalix.core.network.packages.GroupData;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;
import static museum.packages.UserInfoPackage.Action.*;

@RequiredArgsConstructor
public class UserService implements IUserService {

	private final MuseumService museumService;
	private final Logger logger = LoggerUtils.simpleLogger("UserData");
	{ logger.setLevel(Level.FINE); } // Debug

	private final Map<UUID, ServiceUser> userMap = new ConcurrentHashMap<>();

	private MongoAdapter<UserInfo> storageAdapter;

	@Override
	public void enable() {

		storageAdapter = museumService.createStorageAdapter(UserInfo.class, "userData");

		museumService.registerHandler(UserInfoPackage.class, this::handleUserInfo);

		museumService.registerHandler(BulkSaveUserPackage.class, (realm, pckg) -> {
			System.out.println("Received BulkSaveUserPackage from " + realm);

			storageAdapter.save(pckg.getPackages().stream().map(SaveUserPackage::getUserInfo).collect(Collectors.toList()));
		});

	}

	@Override
	public ServiceUser getUser(UUID uuid) {
		return userMap.get(uuid);
	}


	private void handleUserInfo(Realm realm, UserInfoPackage packet) {

		UUID uuid = packet.getUuid();
		UserInfo userInfo = packet.getUserInfo();
		UserInfoPackage.Action action = packet.getAction();

		ServiceUser user = userMap.get(uuid);

		if (action == DATA_REQUEST) {

			if (user == null) {
				storageAdapter.find(uuid).thenAccept(info -> {
					ServiceUser newUser = new ServiceUser(uuid);
					// if (info == null) info = new UserInfo(uuid);
					newUser.setInfo(info);
					newUser.setRealm(realm);
					userMap.put(uuid, newUser);
					packet.setUserInfo(info);
					realm.send(packet);
				});
			} else {
				if (realm == user.getRealm()) {
					user.disconnect("Что-то пошло не так. Бегом к разработчикам!");
					logger.severe("Realm " + realm + " requested user info twice!");
					return;
				}

				logger.fine("Realm " + realm + " requested data, asking " + user.getRealm() + " for a save");
				user.getRealm().send(new UserInfoPackage(uuid, null, DATA_REQUEST));
				user.setTranferRealm(realm);
				user.setTransferTime(System.currentTimeMillis());

			}

		} else if (action == DATA_SYNC) {

			storageAdapter.save(userInfo);
			user.setInfo(userInfo);
			user.setRealm(realm);

		} else if (action == DISCONNECT_SAVE) {

			storageAdapter.save(userInfo);
			user.setInfo(userInfo);

			Realm tranferRealm = user.getTranferRealm();
			if (tranferRealm != null) {

				user.setTransferTime(0);
				user.setTranferRealm(null);
				user.setRealm(tranferRealm);

				tranferRealm.send(packet);

			} else {
				userMap.remove(user.getUuid());
				user.setRealm(null);
			}

		}
	}

	@Override
	public CompletableFuture<List<PlayerTopEntry<Object>>> generateTop(TopPackage.TopType topType, int limit) {
		return storageAdapter.aggregateTop(topType.name().toLowerCase(), limit).thenApplyAsync(entries -> {

			val playerEntries = entries.stream()
					.map(PlayerTopEntry::new).collect(Collectors.toList());

			try {
				List<UUID> uuids = entries.stream()
						.map(entry -> entry.getKey().getUuid())
						.collect(Collectors.toList());

				val groups = ISocketClient.get()
						.<BulkGroupsPackage>writeAndAwaitResponse(new BulkGroupsPackage(uuids))
						.get(5L, TimeUnit.SECONDS)
						.getGroups();

				val map = groups.stream().collect(toMap(GroupData::getUuid, identity()));

				for (val entry : playerEntries) {
					val data = map.get(entry.getKey().getUuid());
					entry.setUserName(data.getUsername());
					entry.setDisplayName(UtilCristalix.createDisplayName(data));
				}
			} catch (Exception ex) {
				ex.printStackTrace();
				playerEntries.forEach(entry -> {
					entry.setUserName("ERROR");
					entry.setDisplayName("ERROR");
				});
			}
			return playerEntries;
		});
	}

}
