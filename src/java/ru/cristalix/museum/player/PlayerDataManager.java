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
import ru.cristalix.museum.client.ClientSocket;
import ru.cristalix.museum.packages.BulkSaveUserPackage;
import ru.cristalix.museum.packages.SaveUserPackage;
import ru.cristalix.museum.packages.UserInfoPackage;
import ru.cristalix.museum.player.prepare.PrepareSteps;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

public class PlayerDataManager implements Listener {

    private final App app;
    private final Map<UUID, User> userMap = new HashMap<>();

    public PlayerDataManager(App app) {
        this.app = app;
        ClientSocket client = app.getClientSocket();
        CoreApi api = CoreApi.get();
        api.bus().register(this, AccountEvent.Load.class, e -> {
            if (e.isCancelled())
                return;
            val uuid = e.getUuid();
            try {
                userMap.put(uuid, new User(client.writeAndAwaitResponse(new UserInfoPackage(uuid, null))
                        .get(5L, TimeUnit.SECONDS)
                        .getUserInfo()
                ));
            } catch (InterruptedException | ExecutionException | TimeoutException ex) {
                e.setCancelReason("Не удалось загрузить статистику о музее.");
                e.setCancelled(true);
                ex.printStackTrace();
            }
        }, 400);
        api.bus().register(this, AccountEvent.Unload.class, e -> {
            val data = userMap.remove(e.getUuid());
            if (data == null) return;
            client.write(new SaveUserPackage(e.getUuid(), data.generateUserInfo()));
        }, 100);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        val player = e.getPlayer();
        val user = userMap.get(player.getUniqueId());

        user.setConnection(((CraftPlayer) player).getHandle().playerConnection);

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
