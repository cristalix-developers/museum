package museum.player;

import clepto.bukkit.B;
import com.google.common.collect.Maps;
import lombok.Setter;
import lombok.val;
import museum.App;
import museum.boosters.BoosterType;
import museum.client.ClientSocket;
import museum.data.BoosterInfo;
import museum.data.UserInfo;
import museum.packages.*;
import museum.player.prepare.*;
import museum.prototype.Managers;
import museum.utils.MultiTimeBar;
import net.minecraft.server.v1_12_R1.PacketPlayOutPlayerInfo;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.spigotmc.event.player.PlayerSpawnLocationEvent;
import ru.cristalix.core.CoreApi;
import ru.cristalix.core.event.AccountEvent;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class PlayerDataManager implements Listener {

	private final App app;
	private final Map<UUID, User> userMap = Maps.newHashMap();
	private final MultiTimeBar timeBar;
	@Setter
	private List<BoosterInfo> globalBoosters = new ArrayList<>(0);
	private final List<Prepare> prepares;

	@SuppressWarnings("deprecation")
	public PlayerDataManager(App app) {
		this.app = app;

		prepares = Arrays.asList(
				BeforePacketHandler.INSTANCE,
				PrepareClientScripts.INSTANCE,
				new PrepareScoreBoard(),
				PrepareTop.INSTANCE,
				PrepareShopBlocks.INSTANCE,
				PreparePlayerBrain.INSTANCE
		);

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
				if (userInfo == null) userInfo = DefaultElements.createNewUserInfo(uuid);
				// Добавление дефолтных значений, которых не было в самом начале
				if (userInfo.getPrefixes() == null) userInfo.setPrefixes(new ArrayList<>());
				if (userInfo.getDonates() == null) userInfo.setDonates(new ArrayList<>(1));
				userMap.put(uuid, new User(userInfo));
			} catch (Exception ex) {
				event.setCancelReason("Не удалось загрузить статистику о музее.");
				event.setCancelled(true);
				ex.printStackTrace();
			}
		}, 400);
		api.bus().register(this, AccountEvent.Unload.class, event -> {
			val data = userMap.remove(event.getUuid());
			if (data == null)
				return;
			val info = data.generateUserInfo();
			info.setTimePlayed(info.getTimePlayed() + System.currentTimeMillis() - data.getEnterTime());
			client.write(new SaveUserPackage(event.getUuid(), info));
		}, 100);
		client.registerHandler(GlobalBoostersPackage.class, pckg -> globalBoosters = pckg.getBoosters());
		client.registerHandler(ExtraDepositUserPackage.class, this::handleExtraDeposit);
		this.timeBar = new MultiTimeBar(
				() -> new ArrayList<>(globalBoosters),
				5L, TimeUnit.SECONDS, () -> null
		);
	}

	@EventHandler
	public void onSpawn(PlayerSpawnLocationEvent event) {
		event.setSpawnLocation(Managers.museum.getPrototype("main").getSpawn());
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		val player = (CraftPlayer) event.getPlayer();

		player.setResourcePack("", "");
		player.setWalkSpeed(.35F);

		timeBar.onJoin(player.getUniqueId());
		val user = userMap.get(player.getUniqueId());

		user.setConnection(player.getHandle().playerConnection);
		user.setPlayer(player);

		player.setGameMode(GameMode.ADVENTURE);
		user.setState(user.getState()); // Загрузка музея
		player.setPlayerTime(user.getInfo().isDarkTheme() ? 12000 : 21000, false);

		B.postpone(1, () -> prepares.forEach(prepare -> prepare.execute(user, app)));

		event.setJoinMessage(null);
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerLogin(PlayerLoginEvent event) {
		if (event.getResult() != PlayerLoginEvent.Result.ALLOWED)
			userMap.remove(event.getPlayer().getUniqueId());
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerQuit(PlayerQuitEvent event) {
		val removePlayer = new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER, ((CraftPlayer) event.getPlayer()).getHandle());
		for (Player player : Bukkit.getOnlinePlayers())
			((CraftPlayer) player).getHandle().playerConnection.sendPacket(removePlayer);
		timeBar.onQuit(event.getPlayer().getUniqueId());
		event.setQuitMessage(null);
	}

	public double calcMultiplier(UUID uuid, BoosterType type) {
		val user = userMap.get(uuid);
		if (user == null)
			return 1;
		return user.calcMultiplier(type) + globalBoosters.stream()
				.filter(booster -> booster.getType() == type && booster.getUntil() > System.currentTimeMillis())
				.mapToDouble(booster -> booster.getMultiplier() - 1.0)
				.sum();
	}

	public double calcGlobalMultiplier(BoosterType type) {
		return 1F + globalBoosters.stream()
				.filter(booster -> booster.getType() == type && booster.getUntil() > System.currentTimeMillis())
				.mapToDouble(booster -> booster.getMultiplier() - 1.0)
				.sum();
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

	public Collection<User> getUsers() {
		return userMap.values();
	}

	private void handleExtraDeposit(ExtraDepositUserPackage pckg) {
		User user = userMap.get(pckg.getUser());
		if (user != null) {
			if (pckg.getSum() != null)
				user.setMoney(user.getMoney() + pckg.getSum());
		}
	}
}
