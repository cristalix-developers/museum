package ru.func.museum.listener;

import lombok.AllArgsConstructor;
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
import ru.func.museum.App;
import ru.func.museum.MongoManager;
import ru.func.museum.player.prepare.PreparePlayer;

import java.util.concurrent.ExecutionException;

/**
 * @author func 06.06.2020
 * @project Museum
 */
@AllArgsConstructor
public class PlayerConnectionListener implements Listener {

    private App app;

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        val player = e.getPlayer();
        val archaeologist = app.getArchaeologistMap().get(player.getUniqueId());

        archaeologist.setConnection(((CraftPlayer) player).getHandle().playerConnection);

        for (val prepare : PreparePlayer.values())
            prepare.getPrepare().execute(player, archaeologist, app);

        e.setJoinMessage(null);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerLogin(PlayerLoginEvent e) {
        if (e.getResult() != PlayerLoginEvent.Result.ALLOWED)
            app.getArchaeologistMap().remove(e.getPlayer().getUniqueId());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void preLoadPlayerEvent(AsyncPlayerPreLoginEvent e) throws ExecutionException, InterruptedException {
        if (e.getLoginResult() != AsyncPlayerPreLoginEvent.Result.ALLOWED)
            return;

        val uuid = e.getUniqueId();
        val archaeologist = MongoManager.load(e.getName(), uuid.toString()).get(); // Block method

        app.getArchaeologistMap().put(uuid, archaeologist);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        e.setQuitMessage(null);

        val uuid = e.getPlayer().getUniqueId();
        var archaeologist = app.getArchaeologistMap().get(uuid);

        archaeologist.setBreakLess(0);
        archaeologist.setOnExcavation(false);

        MongoManager.save(archaeologist);

        app.getArchaeologistMap().remove(uuid);
    }
}
