package museum;

import clepto.bukkit.B;
import clepto.bukkit.Lemonade;
import clepto.bukkit.gui.GuiEvents;
import clepto.cristalix.Cristalix;
import clepto.cristalix.WorldMeta;
import groovy.lang.Script;
import lombok.Getter;
import lombok.val;
import me.func.mod.Anime;
import me.func.mod.Kit;
import museum.boosters.BoosterType;
import museum.client.ClientSocket;
import museum.command.AdminCommand;
import museum.command.MuseumCommands;
import museum.cosmos.boer.BoerManager;
import museum.donate.DonateType;
import museum.international.CrystalExcavations;
import museum.international.International;
import museum.international.Market;
import museum.international.Shop;
import museum.misc.MuseumChatService;
import museum.misc.PlacesMechanic;
import museum.museum.map.SubjectType;
import museum.packages.*;
import museum.player.PlayerDataManager;
import museum.player.User;
import museum.prototype.Managers;
import museum.ticker.detail.Auction;
import museum.ticker.detail.FountainHandler;
import museum.ticker.detail.WayParticleHandler;
import museum.ticker.top.TopManager;
import museum.util.MapLoader;
import museum.visitor.VisitorHandler;
import museum.worker.WorkerUtil;
import net.md_5.bungee.chat.ComponentSerializer;
import net.minecraft.server.v1_12_R1.World;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.craftbukkit.v1_12_R1.CraftWorld;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import ru.cristalix.core.CoreApi;
import ru.cristalix.core.chat.IChatService;
import ru.cristalix.core.permissions.IPermissionService;
import ru.cristalix.core.realm.IRealmService;
import ru.cristalix.core.realm.RealmStatus;
import ru.cristalix.core.scoreboard.IScoreboardService;
import ru.cristalix.core.scoreboard.ScoreboardService;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

@Getter
public final class App extends JavaPlugin {
    @Getter
    public static App app;

    public PlayerDataManager playerDataManager;
    private TopManager topManager;
    private ClientSocket clientSocket;
    private WorldMeta map;
    private YamlConfiguration configuration;

    private Shop shop;
    private Market market;
    private International crystal;

    @Override
    public void onEnable() {
        B.plugin = App.app = this;
        B.events(new PhysicsDisabler());

        // Добавление админ-команд
        AdminCommand.init(this);

        // Подкючение к Netty сервису / Управляет конфигами, кастомными пакетами, всей data
        String museumServiceHost = getEnv("MUSEUM_SERVICE_HOST", "127.0.0.1");
        int museumServicePort = Integer.parseInt(getEnv("MUSEUM_SERVICE_PORT", "14653"));
        String museumServicePassword = getEnv("MUSEUM_SERVICE_PASSWORD", "12345");

        this.clientSocket = new ClientSocket(
                museumServiceHost,
                museumServicePort,
                museumServicePassword,
                Cristalix.getRealmString()
        );
        this.clientSocket.connect();
        this.clientSocket.registerHandler(BroadcastTitlePackage.class, pckg -> {
            String[] data = pckg.getData();
            Bukkit.getOnlinePlayers().forEach(pl -> pl.sendTitle(data[0], data[1], pckg.getFadeIn(), pckg.getStay(), pckg.getFadeOut()));
        });
        this.clientSocket.registerHandler(BroadcastMessagePackage.class, pckg -> Bukkit.getOnlinePlayers()
                .forEach(pl -> pl.sendMessage(ComponentSerializer.parse(pckg.getJsonMessage()))));
        this.clientSocket.registerHandler(TargetMessagePackage.class, pckg -> {
            pckg.getUsers().stream()
                    .map(Bukkit::getPlayer)
                    .filter(Objects::nonNull)
                    .forEach(pl -> pl.sendMessage(ComponentSerializer.parse(pckg.getJsonMessage())));
        });

        // Регистрация Core сервисов
        val core = CoreApi.get();
        core.unregisterService(IChatService.class);
        core.registerService(IChatService.class, new MuseumChatService(IPermissionService.get(), getServer()));
        core.registerService(IScoreboardService.class, new ScoreboardService());

        Anime.include(Kit.NPC);

        // Регистрация обработчика пакета конфига
        clientSocket.registerHandler(ConfigurationsPackage.class, this::fillConfigurations);

        requestConfigurations();

        // Прогрузка Groovy-скриптов
        try (val reader = new BufferedReader(new InputStreamReader(getResource("groovyScripts")))) {
            while (true) {
                String line = reader.readLine();
                if (line == null || line.isEmpty()) break;
                try {
                    Class<?> scriptClass = Class.forName(line);
                    if (!Script.class.isAssignableFrom(scriptClass)) continue;
                    readScript(scriptClass);
                } catch (ClassNotFoundException ignored) {
                }
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }

        // Загрузка основного мира
        map = MapLoader.load("prod1");
        MapLoader.interceptChunkWriter(this, getNMSWorld());
        crystal = new CrystalExcavations(this);

        // Загрузга всех построек (витрины/коллекторы), мэнеджеров
        SubjectType.init();
        Managers.init();
        clepto.bukkit.menu.Guis.init();

        // Класс управляющий игроками
        this.playerDataManager = new PlayerDataManager(this);

        // Прогрузка мэнеджера топа
        topManager = new TopManager(this);

        // Получение бустеров
        requestBoosters();

        // Инициализация команд
        new MuseumCommands(this);
        this.shop = new Shop(this);
        this.market = new Market(this);

        // Регистрация обработчиков событий
        B.events(
                playerDataManager,
                new GuiEvents()
        );

        // Инициализация NPC и точек сбора
        WorkerUtil.init(this);
        PlacesMechanic.init(this);

        // Обработка каждого тика
        new TickTimerHandler(this, Arrays.asList(
                new FountainHandler(this),
                new WayParticleHandler(this),
                topManager,
                new Auction(),
                new BoerManager()
        ), clientSocket, playerDataManager).runTaskTimer(this, 0, 1);

        // Постоянный перерасчет множителя кол-ва посетителей музея
        VisitorHandler.init(this, () -> (int) Math.ceil(3F * playerDataManager.calcGlobalMultiplier(BoosterType.VILLAGER)));

        // Вывод сервера меню
        val realm = IRealmService.get().getCurrentRealmInfo();
        realm.setLobbyServer(true);
        realm.setStatus(RealmStatus.WAITING_FOR_PLAYERS);
        realm.setGroupName("Музей");
        IScoreboardService.get().getServerStatusBoard().setDisplayName("§fМузей #§b" + realm.getRealmId().getId());
    }

    @Override
    public void onDisable() {
        clientSocket.write(playerDataManager.bulk(true));
        try {
            Thread.sleep(1000L); // Если вдруг он не успеет написать в сокет(хотя вряд ли, конечно)
        } catch (Exception exception) {
            exception.printStackTrace();
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
        return clientSocket.writeAndAwaitResponse(new UserTransactionPackage(user, donate, null))
                .thenApply(UserTransactionPackage::getResponse);
    }

    private InputStreamReader reader(String base64) {
        return new InputStreamReader(new ByteArrayInputStream(Base64.getDecoder().decode(base64)));
    }

    public CraftWorld getWorld() {
        return map.getWorld();
    }

    public Collection<User> getUsers() {
        return playerDataManager.getUsers();
    }

    private String getEnv(String name, String defaultValue) {
        String field = System.getenv(name);
        if (field == null || field.isEmpty()) {
            System.out.println("No " + name + " environment variable specified!");
            field = defaultValue;
        }
        return field;
    }

    private void requestConfigurations() {
        try {
            RequestConfigurationsPackage pckg = clientSocket.writeAndAwaitResponse(new RequestConfigurationsPackage())
                    .get(3L, TimeUnit.SECONDS);
            fillConfigurations(new ConfigurationsPackage(pckg.getConfigData(), pckg.getItemsData()));
        } catch (Exception exception) {
            exception.printStackTrace();
            Bukkit.getLogger().severe("We can't receive museum configurations! Retry in 3sec");
            try {
                Thread.sleep(3000L);
            } catch (InterruptedException interruptedException) {
                interruptedException.printStackTrace();
                Thread.currentThread().interrupt();
            }
            requestConfigurations();
        }
    }

    private void fillConfigurations(ConfigurationsPackage pckg) {
        YamlConfiguration itemsConfig = YamlConfiguration.loadConfiguration(reader(pckg.getItemsData()));
        itemsConfig.getKeys(false)
                .forEach(key -> Lemonade.parse(itemsConfig.getConfigurationSection(key)).register(key));

        this.configuration = YamlConfiguration.loadConfiguration(reader(pckg.getConfigData()));
    }

    private void requestBoosters() {
        try {
            RequestGlobalBoostersPackage pckg = clientSocket.writeAndAwaitResponse(new RequestGlobalBoostersPackage())
                    .get(3L, TimeUnit.SECONDS);
            playerDataManager.setGlobalBoosters(pckg.getBoosters());
        } catch (Exception exception) {
            exception.printStackTrace();
            Bukkit.getLogger().severe("We can't get boosters! Retry in 3sec");
            try {
                Thread.sleep(3000L);
            } catch (InterruptedException interruptedException) {
                interruptedException.printStackTrace();
                Thread.currentThread().interrupt();
            }
            requestBoosters();
        }
    }

    private void readScript(Class<?> scriptClass) {
        try {
            Script script = (Script) scriptClass.newInstance();
            script.run();
        } catch (Exception exception) {
            Bukkit.getLogger().log(Level.SEVERE, "An error occurred while running script '" + scriptClass.getName() + "':", exception);
        }
    }
}
