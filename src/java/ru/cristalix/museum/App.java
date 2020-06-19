package ru.cristalix.museum;

import clepto.bukkit.B;
import clepto.bukkit.Lemonade;
import ru.cristalix.museum.museum.MuseumEvents;
import ru.cristalix.museum.player.PlayerDataManager;
import ru.cristalix.museum.museum.subject.skeleton.SkeletonManager;
import lombok.Getter;
import net.minecraft.server.v1_12_R1.World;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import ru.cristalix.core.CoreApi;
import ru.cristalix.core.inventory.IInventoryService;
import ru.cristalix.core.inventory.InventoryService;
import ru.cristalix.core.scoreboard.IScoreboardService;
import ru.cristalix.core.scoreboard.ScoreboardService;
import ru.cristalix.museum.command.MuseumCommand;
import ru.cristalix.museum.player.User;
import ru.cristalix.museum.excavation.ExcavationManager;
import ru.cristalix.museum.util.PassiveEvents;
import ru.cristalix.museum.museum.map.MuseumManager;

import java.io.InputStreamReader;
import java.util.UUID;

@Getter
public final class App extends JavaPlugin {

	@Getter
    private static App app;

    private PlayerDataManager playerDataManager;
    private ServiceConnector serviceConnector;
    private MuseumManager museumManager;
    private SkeletonManager skeletonManager;
	private ExcavationManager excavationManager;

	@Override
    public void onEnable() {
		B.plugin = App.app = this;

		this.playerDataManager = new PlayerDataManager(this);
		this.serviceConnector = new ServiceConnector(this);
		this.museumManager = new MuseumManager(this);
		this.skeletonManager = new SkeletonManager(museumManager);
		this.excavationManager = new ExcavationManager(this, museumManager);

        CoreApi.get().registerService(IScoreboardService.class, new ScoreboardService());
        CoreApi.get().registerService(IInventoryService.class, new InventoryService());

		YamlConfiguration itemsConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(getResource("items.yml")));
		for (String key : itemsConfig.getKeys(false)) {
			Lemonade.parse(itemsConfig.getConfigurationSection(key)).register(key);
		}

		B.events(
				playerDataManager,
                new PassiveEvents(),
//                new MuseumItemHandler(this),
//                new ManipulatorHandler(this),
                new PlayerDataManager(this),
                new MuseumEvents(this)
        );

//        VisitorManager visitorManager = new VisitorManager(HallTemplateType.DEFAULT.getHallTemplate().getCollectorRoute());
//        visitorManager.clear();
//        visitorManager.spawn(new Location(museumManager.getWorld(), -91, 90, 250), 20);

        Bukkit.getPluginCommand("museum").setExecutor(new MuseumCommand(this));
//        Bukkit.getPluginCommand("visitor").setExecutor(new VisitorCommand(visitorManager));

//        new BukkitRunnable() {
//            @Override
//            public void run() {
//                var time = System.currentTimeMillis();
//                val visitedPoint = visitorManager.getVictimFutureLocation();
//
//                for (Player player : Bukkit.getOnlinePlayers()) {
//                    val user = getUser(player.getUniqueId());
//
//                    if (user.getExcavation() != null) continue;
//
//                    if (visitedPoint != null && time % 5 == 0) {
//                        Coin coin = new Coin(visitedPoint);
//                        coin.create(user.getConnection());
//                        user.getCoins().add(coin);
//                    }
//
//                    user.getCurrentMuseum().getCollectors()
//                            .forEach(collector -> collector.move(user, time));
//
//                    // Если монеты устарели, что бы не копились на клиенте, удаляю
//                    user.getCoins().removeIf(coin -> {
//                        if (coin.getTimestamp() + Coin.SECONDS_LIVE * 1000 < time) {
//                            coin.remove(user.getConnection());
//                            return true;
//                        }
//                        return false;
//                    });
//                }
//
//            }
//        }.runTaskTimerAsynchronously(this, 0, 1);
    }

    public User getUser(UUID uuid) {
    	return playerDataManager.getUser(uuid);
	}

	public World getNMSWorld() {
		return getMuseumManager().getWorld().getHandle();
	}

	public User getUser(Player player) {
		return getUser(player.getUniqueId());
	}

}
