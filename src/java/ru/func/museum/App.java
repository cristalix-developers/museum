package ru.func.museum;

import clepto.bukkit.B;
import com.google.common.collect.Maps;
import lombok.Getter;
import lombok.val;
import lombok.var;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import ru.cristalix.core.CoreApi;
import ru.cristalix.core.inventory.IInventoryService;
import ru.cristalix.core.inventory.InventoryService;
import ru.cristalix.core.scoreboard.IScoreboardService;
import ru.cristalix.core.scoreboard.ScoreboardService;
import ru.func.museum.command.MuseumCommand;
import ru.func.museum.command.VisitorCommand;
import ru.func.museum.element.deserialized.EntityDeserializer;
import ru.func.museum.element.deserialized.MuseumEntity;
import ru.func.museum.excavation.Excavation;
import ru.func.museum.listener.*;
import ru.func.museum.museum.coin.AbstractCoin;
import ru.func.museum.museum.coin.Coin;
import ru.func.museum.museum.hall.template.HallTemplateType;
import ru.func.museum.player.Archaeologist;
import ru.func.museum.visitor.VisitorManager;

import java.util.Arrays;
import java.util.Map;
import java.util.UUID;

public final class App extends JavaPlugin {

    @Getter
    private static App app;
    @Getter
    private final Map<UUID, Archaeologist> archaeologistMap = Maps.newHashMap();
    @Getter
    private MuseumEntity[] museumEntities;

    @Override
    public void onEnable() {
		B.plugin = App.app = this;

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

        VisitorManager visitorManager = new VisitorManager(HallTemplateType.DEFAULT.getHallTemplate().getCollectorRoute());
        visitorManager.clear();
        visitorManager.spawn(new Location(Excavation.WORLD, -91, 90, 250), 20);

        Bukkit.getPluginCommand("museum").setExecutor(new MuseumCommand(this));
        Bukkit.getPluginCommand("visitor").setExecutor(new VisitorCommand(visitorManager));

        new BukkitRunnable() {
            @Override
            public void run() {
                var time = System.currentTimeMillis();
                val visitedPoint = visitorManager.getVictimFutureLocation();

                for (Player player : Bukkit.getOnlinePlayers()) {
                    val archaeologist = archaeologistMap.get(player.getUniqueId());

                    if (archaeologist.isOnExcavation()) {
                        continue;
                    }

                    if (visitedPoint != null && time % 5 == 0) {
                        AbstractCoin coin = new Coin(visitedPoint);
                        coin.create(archaeologist.getConnection());
                        archaeologist.getCoins().add(coin);
                    }

                    archaeologist.getCurrentMuseum().getHalls()
                            .forEach(hall -> hall.moveCollector(archaeologist, player, time));

                    // Если монеты устарели, что бы не копились на клиенте, удаляю
                    archaeologist.getCoins().removeIf(coin -> {
                        if (coin.getTimestamp() + AbstractCoin.SECONDS_LIVE * 1000 < time) {
                            coin.remove(archaeologist.getConnection());
                            return true;
                        }
                        return false;
                    });
                }

            }
        }.runTaskTimerAsynchronously(this, 0, 1);
    }
}
