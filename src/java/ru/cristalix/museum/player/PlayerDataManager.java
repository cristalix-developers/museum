package ru.cristalix.museum.player;

import clepto.bukkit.B;
import lombok.val;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import ru.cristalix.core.CoreApi;
import ru.cristalix.core.event.AccountEvent;
import ru.cristalix.museum.App;
import ru.cristalix.museum.boosters.BoosterType;
import ru.cristalix.museum.client.ClientSocket;
import ru.cristalix.museum.data.BoosterInfo;
import ru.cristalix.museum.data.MuseumInfo;
import ru.cristalix.museum.data.PickaxeType;
import ru.cristalix.museum.data.UserInfo;
import ru.cristalix.museum.data.subject.SubjectInfo;
import ru.cristalix.museum.museum.map.MuseumPrototype;
import ru.cristalix.museum.packages.*;
import ru.cristalix.museum.player.prepare.PrepareSteps;
import ru.cristalix.museum.prototype.Managers;
import ru.cristalix.museum.utils.MultiTimeBar;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

public class PlayerDataManager implements Listener {

	private final App app;
	private final Map<UUID, User> userMap = new HashMap<>();
	private final MultiTimeBar timeBar;
	private List<BoosterInfo> globalBoosters = new ArrayList<>(0);

	public PlayerDataManager(App app) {
		this.app = app;

		ClientSocket client = app.getClientSocket();
		CoreApi api = CoreApi.get();

		api.bus().register(this, AccountEvent.Load.class, event -> {
			if (event.isCancelled())
				return;
			val uuid = event.getUuid();
			try {
				UserInfoPackage userInfoPackage = client.writeAndAwaitResponse(new UserInfoPackage(uuid))
						.get(5L, TimeUnit.SECONDS);
				UserInfo userInfo = userInfoPackage.getUserInfo();
				if (userInfo == null) {
					MuseumPrototype proto = Managers.museum.getPrototype("main");
					MuseumInfo startMuseum = new MuseumInfo(
							proto.getAddress(),
							"Музей археологии",
							new Date(),
							3
					);

					userInfo = new UserInfo(
							uuid,
							0,
							1000.0,
							PickaxeType.DEFAULT,
							Collections.singletonList(startMuseum),
							new ArrayList<>(),
							new ArrayList<>(),
							0,
							0,
							new ArrayList<>(),
                            new ArrayList<>()
					);
					for (SubjectInfo subject : proto.getDefaultSubjects()) {
						userInfo.getSubjectInfos().add(subject.clone());
					}
				} else {
					// No dynamic constructor :( thx java
					userInfo.setDonates(userInfo.getDonates() == null ?
							new ArrayList<>(1) :
							new ArrayList<>(userInfo.getDonates())
					);
				}
				userMap.put(uuid, new User(userInfo));
			} catch (InterruptedException | ExecutionException | TimeoutException ex) {
				event.setCancelReason("Не удалось загрузить статистику о музее.");
				event.setCancelled(true);
				ex.printStackTrace();
			}
		}, 400);
		api.bus().register(this, AccountEvent.Unload.class, event -> {
			val data = userMap.remove(event.getUuid());
			if (data == null)
				return;
			client.write(new SaveUserPackage(event.getUuid(), data.generateUserInfo()));
		}, 100);
		client.registerHandler(GlobalBoostersPackage.class, pckg -> globalBoosters = pckg.getBoosters());
		client.registerHandler(ExtraDepositUserPackage.class, pckg -> {
			User user = userMap.get(pckg.getUser());
			if (user != null) {
				if (pckg.getSum() != null)
					user.setMoney(user.getMoney() + pckg.getSum());
				if (pckg.getSeconds() != null) {
					double result = pckg.getSeconds() * user.getCurrentMuseum().getIncome(); // Типа того
					user.setMoney(user.getMoney() + result);
				}
			}
		});
		this.timeBar = new MultiTimeBar(
				() -> new ArrayList<>(globalBoosters),
				5L, TimeUnit.SECONDS, () -> null
		);
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent e) {
		val player = (CraftPlayer) e.getPlayer();
		timeBar.onJoin(player.getUniqueId());
		val user = userMap.get(player.getUniqueId());

		user.setConnection(player.getHandle().playerConnection);
		user.setPlayer(player);

		B.postpone(5, () -> {
			for (val prepare : PrepareSteps.values())
				prepare.getPrepare().execute(user, app);
		});

		e.setJoinMessage(null);
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerLogin(PlayerLoginEvent e) {
		if (e.getResult() != PlayerLoginEvent.Result.ALLOWED)
			userMap.remove(e.getPlayer().getUniqueId());
	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent e) {
		timeBar.onQuit(e.getPlayer().getUniqueId());
		e.setQuitMessage(null);
	}

	public double calcMultiplier(UUID user, BoosterType type) {
		// todo: useless method
		return userMap.get(user).calcMultiplier(type) + globalBoosters.stream().filter(booster -> booster.getType() == type && booster.getUntil() > System.currentTimeMillis()).mapToDouble(
				booster -> booster.getMultiplier() - 1.0).sum();
	}

	public User getUser(UUID uuid) {
		return userMap.get(uuid);
	}

	public BulkSaveUserPackage bulk(boolean remove) {
		return new BulkSaveUserPackage(Bukkit.getOnlinePlayers().stream().map(pl -> {
			val uuid = pl.getUniqueId();
			User user = remove ? userMap.remove(uuid) : userMap.get(uuid);
			if (user == null)
				return null;
			return new SaveUserPackage(uuid, user.generateUserInfo());
		}).filter(Objects::nonNull).collect(Collectors.toList()));
	}

}
