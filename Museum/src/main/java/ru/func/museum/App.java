package ru.func.museum;

import com.google.common.collect.Maps;
import lombok.Getter;
import org.bukkit.Bukkit;
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
import ru.func.museum.listener.*;
import ru.func.museum.player.Archaeologist;

import java.util.Arrays;
import java.util.Map;
import java.util.UUID;

public final class App extends JavaPlugin {

    @Getter
    private static App app;
    @Getter
    private Map<UUID, Archaeologist> archaeologistMap = Maps.newHashMap();
    @Getter
    private MuseumEntity[] museumEntities;

    @Override
    public void onEnable() {
        app = this;

        CoreApi.get().registerService(IScoreboardService.class, new ScoreboardService());
        CoreApi.get().registerService(IInventoryService.class, new InventoryService());

        Arrays.asList(
                new CancelEvent(),
                new MuseumItemHandler(this),
                new ManipulatorHandler(this),
                new PlayerConnectionListener(this),
                new MoveListener(this)
        ).forEach(listener -> Bukkit.getPluginManager().registerEvents(listener, this));

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
}
