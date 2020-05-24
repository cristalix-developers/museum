package ru.func.museum;

import com.google.common.collect.Maps;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import ru.func.museum.player.Archaeologist;

import java.util.Map;
import java.util.UUID;

public final class App extends JavaPlugin implements Listener {

    private Map<UUID, Archaeologist> archaeologistMap = Maps.newHashMap();

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);

        MongoManager.connect(
                getConfig().getString("uri"),
                getConfig().getString("database"),
                getConfig().getString("collection")
        );
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();
        archaeologistMap.put(player.getUniqueId(), MongoManager.load(player));
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        archaeologistMap.remove(UUID.fromString(
                MongoManager.save(archaeologistMap.get(e.getPlayer().getUniqueId())).getUuid()
        ));
    }
}
