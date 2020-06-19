package ru.cristalix.museum.player;

import lombok.RequiredArgsConstructor;
import lombok.val;
import lombok.var;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import ru.cristalix.museum.App;
import ru.cristalix.museum.data.UserInfo;
import ru.cristalix.museum.player.User;
import ru.cristalix.museum.player.prepare.PrepareSteps;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RequiredArgsConstructor
public class PlayerDataManager implements Listener {

	private final App app;
	private final Map<UUID, User> userMap = new HashMap<>();

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent e) {
		val player = e.getPlayer();
		val user = userMap.get(player.getUniqueId());

		user.setConnection(((CraftPlayer) player).getHandle().playerConnection);

		for (val prepare : PrepareSteps.values())
			prepare.getPrepare().execute(user, app);

		e.setJoinMessage(null);
	}

	@EventHandler (priority = EventPriority.MONITOR)
	public void onPlayerLogin(PlayerLoginEvent e) {
		if (e.getResult() != PlayerLoginEvent.Result.ALLOWED)
			userMap.remove(e.getPlayer().getUniqueId());
	}

	@EventHandler (priority = EventPriority.MONITOR)
	public void preLoadPlayerEvent(AsyncPlayerPreLoginEvent e) {

		if (e.getLoginResult() != AsyncPlayerPreLoginEvent.Result.ALLOWED || e.getPlayerProfile() == null)
			return;

		val uuid = e.getUniqueId();
		UserInfo userInfo = app.getServiceConnector().loadUserSync(uuid);

		userMap.put(uuid, new User(userInfo));
	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent e) {
		e.setQuitMessage(null);

		val uuid = e.getPlayer().getUniqueId();
		var user = userMap.get(uuid);

		app.getServiceConnector().saveUser(uuid, user.generateUserInfo());

		userMap.remove(uuid);
	}

	public User getUser(UUID uuid) {
		return userMap.get(uuid);
	}

}
