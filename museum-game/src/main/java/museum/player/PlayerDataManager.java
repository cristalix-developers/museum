package museum.player;

import clepto.bukkit.B;
import com.destroystokyo.paper.event.player.PlayerInitialSpawnEvent;
import lombok.val;
import museum.App;
import museum.boosters.BoosterType;
import museum.client.ClientSocket;
import museum.data.BoosterInfo;
import museum.data.UserInfo;
import museum.museum.Museum;
import museum.packages.*;
import museum.player.prepare.*;
import museum.utils.MultiTimeBar;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.spigotmc.event.player.PlayerSpawnLocationEvent;
import ru.cristalix.core.CoreApi;
import ru.cristalix.core.event.AccountEvent;

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
	public static final PotionEffect NIGHT_VISION = new PotionEffect(PotionEffectType.NIGHT_VISION, 65536, 10, false, false);

	@SuppressWarnings("deprecation")
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
				if (userInfo == null) userInfo = DefaultElements.createNewUserInfo(uuid);
				if (userInfo.getDonates() == null) userInfo.setDonates(new ArrayList<>(1));
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
					double result = pckg.getSeconds() * user.getMuseums().stream().mapToDouble(Museum::getIncome).sum(); // Типа того
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
	public void onPreLogin(AsyncPlayerPreLoginEvent e) {
		try {
			UserInfoPackage userInfoPackage = app.getClientSocket().writeAndAwaitResponse(new UserInfoPackage(e.getUniqueId()))
					.get(5L, TimeUnit.SECONDS);
			UserInfo userInfo = userInfoPackage.getUserInfo();
			if (userInfo == null) userInfo = DefaultElements.createNewUserInfo(e.getUniqueId());
			if (userInfo.getDonates() == null) userInfo.setDonates(new ArrayList<>(1));
			User user = new User(userInfo);
			userMap.put(e.getUniqueId(), user);

			try {
				e.setSpawnLocation(user.getLastLocation());
			} catch (NoSuchMethodError ignored) {
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	@EventHandler
	public void onSpawn(PlayerSpawnLocationEvent e) {
		e.setSpawnLocation(app.getUser(e.getPlayer()).getLastLocation());
	}

	@EventHandler
	public void onSpawn(PlayerInitialSpawnEvent e) {
		e.setSpawnLocation(app.getUser(e.getPlayer()).getLastLocation());
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent e) {
		val player = (CraftPlayer) e.getPlayer();
		timeBar.onJoin(player.getUniqueId());
		val user = userMap.get(player.getUniqueId());

		user.setConnection(player.getHandle().playerConnection);
		user.setPlayer(player);

		B.postpone(2, () -> Arrays.asList(
				BeforePacketHandler.INSTANCE,
				new PrepareJSAnime(),
				(usr, app) -> usr.getPlayer().setWalkSpeed(.33F),
				(usr, app) -> user.setState(user.getState()), // Музей
				new PrepareScoreBoard(),
				(usr, app) -> user.getPlayer().addPotionEffect(NIGHT_VISION),
				(usr, app) -> Bukkit.getOnlinePlayers().forEach(current -> user.getPlayer().hidePlayer(app, current)), // Скрытие игроков
				(usr, app) -> user.getPlayer().setGameMode(GameMode.ADVENTURE), // Режим игры
				new PreparePlayerBrain(app)
		).forEach(prepare -> prepare.execute(user, app)));

		e.setJoinMessage(null);
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerLogin(PlayerLoginEvent e) {
		val player = e.getPlayer();
		if (e.getResult() != PlayerLoginEvent.Result.ALLOWED)
			userMap.remove(player.getUniqueId());
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

	public Collection<User> getUsers() {
		return userMap.values();
	}

}
