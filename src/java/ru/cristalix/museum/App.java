package ru.cristalix.museum;

import clepto.bukkit.B;
import clepto.bukkit.Lemonade;
import clepto.bukkit.gui.GuiEvents;
import clepto.bukkit.gui.Guis;
import clepto.cristalix.Cristalix;
import clepto.cristalix.WorldMeta;
import lombok.Getter;
import lombok.val;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import net.minecraft.server.v1_12_R1.World;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.craftbukkit.v1_12_R1.CraftWorld;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import ru.cristalix.core.CoreApi;
import ru.cristalix.core.chat.IChatService;
import ru.cristalix.core.inventory.IInventoryService;
import ru.cristalix.core.inventory.InventoryService;
import ru.cristalix.core.map.BukkitWorldLoader;
import ru.cristalix.core.map.MapListDataItem;
import ru.cristalix.core.permissions.IPermissionService;
import ru.cristalix.core.realm.IRealmService;
import ru.cristalix.core.scoreboard.IScoreboardService;
import ru.cristalix.core.scoreboard.ScoreboardService;
import ru.cristalix.museum.client.ClientSocket;
import ru.cristalix.museum.command.MuseumCommand;
import ru.cristalix.museum.command.VisitorCommand;
import ru.cristalix.museum.donate.DonateType;
import ru.cristalix.museum.gui.MuseumGuis;
import ru.cristalix.museum.museum.Coin;
import ru.cristalix.museum.museum.MuseumEvents;
import ru.cristalix.museum.museum.map.SubjectType;
import ru.cristalix.museum.museum.subject.CollectorSubject;
import ru.cristalix.museum.packages.*;
import ru.cristalix.museum.player.PlayerDataManager;
import ru.cristalix.museum.player.User;
import ru.cristalix.museum.util.BukkitChatService;
import ru.cristalix.museum.listener.PassiveEvents;
import ru.cristalix.museum.prototype.Managers;
import ru.cristalix.museum.visitor.VisitorManager;

import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.util.Base64;
import java.util.Collections;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Getter
public final class App extends JavaPlugin {

    @Getter
    private static App app;

    private PlayerDataManager playerDataManager;
    private ClientSocket clientSocket;
    private WorldMeta map;

    private YamlConfiguration configuration;

    @Override
    public void onEnable() {
        B.plugin = App.app = this;

        MapListDataItem mapInfo = Cristalix.mapService().getMapByGameTypeAndMapName("MODELS", "Dino")
                .orElseThrow(() -> new RuntimeException("Map museum/main wasn't found in the MapService"));

        try {
            this.map = new WorldMeta(Cristalix.mapService().loadMap(mapInfo.getLatest(), BukkitWorldLoader.INSTANCE).get());
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
        this.map.getWorld().setGameRuleValue("mobGriefing", "false");

        SubjectType.init();
        Managers.init();

        this.clientSocket = new ClientSocket("127.0.0.1", 14653, "gVatjN43AJnbFq36Fa", IRealmService.get().getCurrentRealmInfo().getRealmId().getRealmName());
        clientSocket.connect();
        clientSocket.registerHandler(BroadcastTitlePackage.class, pckg -> {
            String[] data = pckg.getData();
            Bukkit.getOnlinePlayers().forEach(pl -> pl.sendTitle(data[0], data[1], pckg.getFadeIn(), pckg.getStay(), pckg.getFadeOut()));
        });
        clientSocket.registerHandler(BroadcastMessagePackage.class, pckg -> {
            BaseComponent[] msg = ComponentSerializer.parse(pckg.getJsonMessage());
            Bukkit.getOnlinePlayers().forEach(pl -> pl.sendMessage(msg));
        });
        clientSocket.registerHandler(TargetMessagePackage.class, pckg -> {
            BaseComponent[] msg = ComponentSerializer.parse(pckg.getJsonMessage());
            pckg.getUsers().stream().map(Bukkit::getPlayer).filter(Objects::nonNull).forEach(pl -> pl.sendMessage(msg));
        });
        this.playerDataManager = new PlayerDataManager(this);

        CoreApi.get().unregisterService(IChatService.class);
        CoreApi.get().registerService(IChatService.class, new BukkitChatService(IPermissionService.get(), getServer()));
        CoreApi.get().registerService(IScoreboardService.class, new ScoreboardService());
        CoreApi.get().registerService(IInventoryService.class, new InventoryService());

        clientSocket.registerHandler(ConfigurationsPackage.class, pckg -> {
            YamlConfiguration itemsConfig = YamlConfiguration.loadConfiguration(reader(pckg.getItemsData()));
            for (String key : itemsConfig.getKeys(false)) {
                Lemonade.parse(itemsConfig.getConfigurationSection(key)).register(key);
            }

            Guis.loadGuis(YamlConfiguration.loadConfiguration(reader(pckg.getGuisData())));

            this.configuration = YamlConfiguration.loadConfiguration(reader(pckg.getConfigData()));
        });

        new MuseumGuis(this);

        B.events(
                playerDataManager,
                new PassiveEvents(),
                new MuseumEvents(this),
                new GuiEvents()
        );

        VisitorManager visitorManager = new VisitorManager(Collections.singletonList(new Location(getWorld(), -67, 91, 268)));
        visitorManager.clear();
        visitorManager.spawn(new Location(getWorld(), -67, 91, 268), 20);

        Bukkit.getPluginCommand("museum").setExecutor(new MuseumCommand(this));
        Bukkit.getPluginCommand("visitor").setExecutor(new VisitorCommand(visitorManager));

        new BukkitRunnable() {
            @Override
            public void run() {
                long time = System.currentTimeMillis();
                val visitedPoint = visitorManager.getVictimFutureLocation();

                for (Player player : Bukkit.getOnlinePlayers()) {
                    val user = getUser(player.getUniqueId());
					B.bc("Moving " + user.getPlayer() + "'s collector...");

                    if (user.getExcavation() != null || user.getCurrentMuseum() == null)
                        continue;

                    if (visitedPoint != null) {
                        Coin coin = new Coin(visitedPoint);
                        coin.create(user.getConnection());
                        user.getCoins().add(coin);
                    }
					for (CollectorSubject collector : user.getCurrentMuseum().getSubjects(SubjectType.COLLECTOR)) {
						collector.move(user, time);
						B.bc(visitedPoint.getX() + " " + visitedPoint.getY() + " " + visitedPoint.getZ());
					}

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
        }.runTaskTimer(this, 0, 1);

        long autoSavePeriod = 20 * 60 * 3;
        Bukkit.getScheduler().runTaskTimerAsynchronously(this, () ->
                        clientSocket.write(playerDataManager.bulk(false)), autoSavePeriod, autoSavePeriod);
    }

    @Override
    public void onDisable() {
        clientSocket.write(playerDataManager.bulk(true));
        try {
            Thread.sleep(1000L); // Если вдруг он не успеет написать в сокет(хотя вряд ли, конечно)
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public FileConfiguration getConfig() {
        return configuration;
    }

    public User getUser(UUID uuid) {
        return playerDataManager.getUser(uuid);
    }

    public World getNMSWorld() {
        return map.getWorld().getHandle();
    }

    public User getUser(Player player) {
        return getUser(player.getUniqueId());
    }

    public CompletableFuture<UserTransactionPackage.TransactionResponse> processDonate(UUID user, DonateType donate) {
        return getClientSocket().writeAndAwaitResponse(new UserTransactionPackage(user, donate, null)).thenApply(UserTransactionPackage::getResponse);
    }

    private InputStreamReader reader(String base64) {
        return new InputStreamReader(new ByteArrayInputStream(Base64.getDecoder().decode(base64)));
    }

    public CraftWorld getWorld() {
        return map.getWorld();
    }

}
