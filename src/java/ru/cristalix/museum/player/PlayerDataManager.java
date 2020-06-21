package ru.cristalix.museum.player;

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
import ru.cristalix.museum.boosters.Booster;
import ru.cristalix.museum.boosters.BoosterType;
import ru.cristalix.museum.client.ClientSocket;
import ru.cristalix.museum.data.MuseumInfo;
import ru.cristalix.museum.data.PickaxeType;
import ru.cristalix.museum.data.UserInfo;
import ru.cristalix.museum.packages.BulkSaveUserPackage;
import ru.cristalix.museum.packages.GlobalBoostersPackage;
import ru.cristalix.museum.packages.SaveUserPackage;
import ru.cristalix.museum.packages.UserInfoPackage;
import ru.cristalix.museum.player.prepare.PrepareSteps;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

public class PlayerDataManager implements Listener {

	private final App app;
	private final Map<UUID, User> userMap = new HashMap<>();
	private List<Booster> globalBoosters = new ArrayList<>(0);

	public PlayerDataManager(App app) {
		this.app = app;
		ClientSocket client = app.getClientSocket();
		CoreApi api = CoreApi.get();
		api.bus().register(this, AccountEvent.Load.class, e -> {
			if (e.isCancelled())
				return;
			val uuid = e.getUuid();
			try {
				UserInfoPackage userInfoPackage = client.writeAndAwaitResponse(new UserInfoPackage(uuid))
						.get(5L, TimeUnit.SECONDS);
				UserInfo userInfo = userInfoPackage.getUserInfo();
				if (userInfo == null) {
					userInfo = new UserInfo(
							uuid,
							0,
							1000.0,
							PickaxeType.DEFAULT,
							Collections.singletonList(
									new MuseumInfo(
											"main",
											"Музей археологии",
											new Date(),
											3,
											Collections.emptyList(),
											5,
											Collections.emptyList()
									)
							),
							Collections.emptyList(),
							0,
							0
					);
				}
				userMap.put(uuid, new User(userInfo, new ArrayList<>(userInfoPackage.getLocalBoosters())));
			} catch (InterruptedException | ExecutionException | TimeoutException ex) {
				e.setCancelReason("Не удалось загрузить статистику о музее.");
				e.setCancelled(true);
				ex.printStackTrace();
			}
		}, 400);
		api.bus().register(this, AccountEvent.Unload.class, e -> {
			val data = userMap.remove(e.getUuid());
			if (data == null)
				return;
			client.write(new SaveUserPackage(e.getUuid(), data.generateUserInfo()));
		}, 100);
		client.registerHandler(GlobalBoostersPackage.class, pckg -> globalBoosters = pckg.getBoosters());
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent e) {
		val player = e.getPlayer();
		val user = userMap.get(player.getUniqueId());

		user.setConnection(((CraftPlayer) player).getHandle().playerConnection);
		user.setPlayer(player);

		for (val prepare : PrepareSteps.values())
			prepare.getPrepare().execute(user, app);

		e.setJoinMessage(null);
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerLogin(PlayerLoginEvent e) {
		if (e.getResult() != PlayerLoginEvent.Result.ALLOWED)
			userMap.remove(e.getPlayer().getUniqueId());
	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent e) {
		e.setQuitMessage(null);
	}

	public double calcMultiplier(UUID user, BoosterType type) {
		return userMap.get(user).calcMultiplier(type) + globalBoosters.stream().filter(booster -> booster.getType() == type && booster.getUntil() > System.currentTimeMillis()).mapToDouble(booster -> booster.getMultiplier() - 1.0).sum();
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
