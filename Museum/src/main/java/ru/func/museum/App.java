package ru.func.museum;

import com.google.common.collect.Maps;
import lombok.Getter;
import lombok.val;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import ru.cristalix.core.CoreApi;
import ru.cristalix.core.inventory.IInventoryService;
import ru.cristalix.core.inventory.InventoryService;
import ru.cristalix.core.scoreboard.IScoreboardService;
import ru.cristalix.core.scoreboard.ScoreboardService;
import ru.func.museum.element.deserialized.EntityDeserializer;
import ru.func.museum.element.deserialized.MuseumEntity;
import ru.func.museum.player.Archaeologist;
import ru.func.museum.player.prepare.PreparePlayer;

import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

public final class App extends JavaPlugin implements Listener {

    @Getter
    private static App app;
    private Map<UUID, Archaeologist> archaeologistMap = Maps.newHashMap();
    @Getter
    private MuseumEntity[] museumEntities;

    @Override
    public void onEnable() {
        app = this;

        CoreApi.get().registerService(IScoreboardService.class, new ScoreboardService());
        CoreApi.get().registerService(IInventoryService.class, new InventoryService());

        Bukkit.getPluginManager().registerEvents(this, this);

        MongoManager.connect(
                getConfig().getString("uri"),
                getConfig().getString("database"),
                getConfig().getString("collection")
        );

        // Десериализация данных о существах
        museumEntities = new EntityDeserializer().execute(getConfig().getStringList("entity"));
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        val player = e.getPlayer();
        val archaeologist = MongoManager.load(player);
        archaeologistMap.put(player.getUniqueId(), archaeologist);
        Stream.of(PreparePlayer.values())
                .map(PreparePlayer::getPrepare)
                .forEach(prepare -> prepare.execute(player, archaeologist, this));
        e.setJoinMessage(null);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        e.setQuitMessage(null);
        archaeologistMap.remove(UUID.fromString(
                MongoManager.save(archaeologistMap.get(e.getPlayer().getUniqueId())).getUuid()
        ));
    }

    @EventHandler
    public void onItemDrop(PlayerDropItemEvent e) {
        archaeologistMap.get(e.getPlayer().getUniqueId())
                .getLastExcavation()
                .getExcavation()
                .getExcavationGenerator()
                .generateAndShow(e.getPlayer());
    }
}
