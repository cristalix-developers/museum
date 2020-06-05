package ru.func.museum;

import com.google.common.collect.Maps;
import lombok.Getter;
import lombok.val;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.*;
import org.bukkit.plugin.java.JavaPlugin;
import ru.cristalix.core.CoreApi;
import ru.cristalix.core.inventory.IInventoryService;
import ru.cristalix.core.inventory.InventoryService;
import ru.cristalix.core.scoreboard.IScoreboardService;
import ru.cristalix.core.scoreboard.ScoreboardService;
import ru.func.museum.command.MuseumCommand;
import ru.func.museum.element.deserialized.EntityDeserializer;
import ru.func.museum.element.deserialized.MuseumEntity;
import ru.func.museum.excavation.Excavation;
import ru.func.museum.listener.CancelEvent;
import ru.func.museum.listener.MuseumItemHandler;
import ru.func.museum.player.Archaeologist;
import ru.func.museum.player.prepare.PreparePlayer;

import java.util.Map;
import java.util.UUID;

@Getter
public final class App extends JavaPlugin implements Listener {

    @Getter
    private static App app;
    private Map<UUID, Archaeologist> archaeologistMap = Maps.newHashMap();
    private MuseumEntity[] museumEntities;

    @Override
    public void onEnable() {
        app = this;

        CoreApi.get().registerService(IScoreboardService.class, new ScoreboardService());
        CoreApi.get().registerService(IInventoryService.class, new InventoryService());

        Bukkit.getPluginManager().registerEvents(this, this);
        Bukkit.getPluginManager().registerEvents(new CancelEvent(), this);
        Bukkit.getPluginManager().registerEvents(new MuseumItemHandler(this), this);

        MongoManager.connect(
                getConfig().getString("uri"),
                getConfig().getString("database"),
                getConfig().getString("collection")
        );

        // Десериализация данных о существах
        museumEntities = new EntityDeserializer().execute(getConfig().getStringList("entity"));

        Excavation.WORLD.setGameRuleValue("mobGriefing", "false");

        Bukkit.getPluginCommand("museum").setExecutor(new MuseumCommand(this));
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        val player = e.getPlayer();
        for (val prepare : PreparePlayer.values())
            prepare.getPrepare().execute(player, archaeologistMap.get(player.getUniqueId()), this);
        e.setJoinMessage(null);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerLogin(PlayerLoginEvent e) {
        if (e.getResult() != PlayerLoginEvent.Result.ALLOWED)
            archaeologistMap.remove(e.getPlayer().getUniqueId());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void preLoadPlayerEvent(AsyncPlayerPreLoginEvent e) {
        if (e.getLoginResult() != AsyncPlayerPreLoginEvent.Result.ALLOWED)
            return;
        val archaeologist = MongoManager.load(e.getName(), e.getUniqueId().toString());
        archaeologistMap.put(e.getUniqueId(), archaeologist);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        e.setQuitMessage(null);

        val archaeologist = archaeologistMap.get(e.getPlayer().getUniqueId());

        archaeologist.setBreakLess(0);
        archaeologist.setOnExcavation(false);

        MongoManager.save(archaeologist);

        archaeologistMap.remove(e.getPlayer().getUniqueId());
    }
}
