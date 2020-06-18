package ru.func.museum;

import clepto.bukkit.B;
import clepto.bukkit.Lemonade;
import delfikpro.exhibit.ExhibitManager;
import lombok.Getter;
import lombok.val;
import lombok.var;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;
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
import ru.func.museum.excavation.Excavation;
import ru.func.museum.listener.*;
import ru.func.museum.museum.map.MuseumMap;
import ru.func.museum.museum.coin.Coin;
import ru.func.museum.player.User;
import ru.func.museum.visitor.VisitorManager;

import java.io.InputStreamReader;
import java.util.UUID;

@Getter
public final class App extends JavaPlugin {

	@Getter
    private static App app;

    private PlayerDataManager playerDataManager;
    private ServiceConnector serviceConnector;
    private MuseumMap museumMap;
    private ExhibitManager exhibitManager;

    @Override
    public void onEnable() {
		B.plugin = App.app = this;

		this.playerDataManager = new PlayerDataManager(this);
		this.serviceConnector = new ServiceConnector(this);
		this.museumMap = new MuseumMap(this);
		this.exhibitManager = new ExhibitManager(museumMap);

        CoreApi.get().registerService(IScoreboardService.class, new ScoreboardService());
        CoreApi.get().registerService(IInventoryService.class, new InventoryService());

		YamlConfiguration itemsConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(getResource("items.yml")));
		for (String key : itemsConfig.getKeys(false)) {
			Lemonade.parse(itemsConfig.getConfigurationSection(key)).register(key);
		}

		B.events(
				playerDataManager,
                new CancelEvent(),
                new MuseumItemHandler(this),
                new ManipulatorHandler(this),
                new PlayerDataManager(this),
                new MoveListener(this)
        );

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
                    val user = getUser(player.getUniqueId());

                    if (user.isOnExcavation()) {
                        continue;
                    }

                    if (visitedPoint != null && time % 5 == 0) {
                        Coin coin = new Coin(visitedPoint);
                        coin.create(user.getConnection());
                        user.getCoins().add(coin);
                    }

                    user.getCurrentMuseum().getCollectors()
                            .forEach(collector -> collector.move(user, time));

                    // Если монеты устарели, что бы не копились на клиенте, удаляю
                    user.getCoins().removeIf(coin -> {
                        if (coin.getTimestamp() + Coin.SECONDS_LIVE * 1000 < time) {
                            coin.remove(user.getConnection());
                            return true;
                        }
                        return false;
                    });
                }

            }
        }.runTaskTimerAsynchronously(this, 0, 1);
    }

    public User	getUser(UUID uuid) {
    	return playerDataManager.getUser(uuid);
	}

}
