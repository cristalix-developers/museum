package museum;

import clepto.bukkit.B;
import clepto.bukkit.Lemonade;
import clepto.bukkit.gui.GuiEvents;
import clepto.bukkit.gui.Guis;
import clepto.cristalix.mapservice.WorldMeta;
import groovy.lang.Script;
import lombok.Getter;
import lombok.Setter;
import museum.client.ClientSocket;
import museum.command.AdminCommand;
import museum.command.MuseumCommands;
import museum.donate.DonateType;
import museum.gui.MuseumGuis;
import museum.listener.BlockClickHandler;
import museum.listener.MuseumEventHandler;
import museum.listener.PassiveEventBlocker;
import museum.museum.Shop;
import museum.museum.map.SubjectType;
import museum.packages.*;
import museum.player.PlayerDataManager;
import museum.player.User;
import museum.prototype.Managers;
import museum.ticker.detail.FountainHandler;
import museum.util.MapLoader;
import museum.util.MuseumChatService;
import museum.visitor.VisitorHandler;
import museum.worker.WorkerHandler;
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
import ru.cristalix.core.inventory.IInventoryService;
import ru.cristalix.core.inventory.InventoryService;
import ru.cristalix.core.permissions.IPermissionService;
import ru.cristalix.core.realm.IRealmService;
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
	private static App app;

	private PlayerDataManager playerDataManager;
	private ClientSocket clientSocket;
	@Setter
	private WorldMeta map;
	private YamlConfiguration configuration;

	private Shop shop;

	@Override
	public void onEnable() {
		B.plugin = App.app = this;

		// Загрузка мира
		MapLoader.load(this);

		// Добавление админ-команд
		AdminCommand.init(this);

		// Загрузга всех построек (витрины/коллекторы), мэнеджеров
		SubjectType.init();
		Managers.init();
		clepto.bukkit.menu.Guis.init();

		// Подкючение к Netty сервису / Управляет конфигами, кастомными пакетами, всей data
		this.clientSocket = new ClientSocket(
				"127.0.0.1",
				14653,
				"gVatjN43AJnbFq36Fa",
				IRealmService.get().getCurrentRealmInfo().getRealmId().getRealmName()
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
		CoreApi.get().unregisterService(IChatService.class);
		CoreApi.get().registerService(IChatService.class, new MuseumChatService(IPermissionService.get(), getServer()));
		CoreApi.get().registerService(IScoreboardService.class, new ScoreboardService());
		CoreApi.get().registerService(IInventoryService.class, new InventoryService());

		// Регистрация обработчика пакета конфига
		clientSocket.registerHandler(ConfigurationsPackage.class, this::fillConfigurations);

		requestConfigurations();

		// Прогрузка предметов из Groovy-скриптов
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(getResource("groovyScripts")));
			while (true) {
				String line = reader.readLine();
				if (line == null || line.isEmpty()) break;
				Class<?> scriptClass = Class.forName(line);
				if (!Script.class.isAssignableFrom(scriptClass)) continue;
				Script script = (Script) scriptClass.newInstance();
				try {
					script.run();
				} catch (Throwable throwable) {
					Bukkit.getLogger().log(Level.SEVERE, "An error occurred while running script '" + scriptClass.getName() + "':", throwable);
				}
			}
			Class<?> museumItems = Class.forName("museum.config.items");
			museumItems.getMethod("run").invoke(museumItems.newInstance());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		// Класс управляющий игроками
		this.playerDataManager = new PlayerDataManager(this);

		// Инициализация команд
		new MuseumCommands(this);
		this.shop = new Shop(this);

		// Регистрация обработчиков событий
		B.events(
				playerDataManager,
				new PassiveEventBlocker(),
				new MuseumEventHandler(this),
				new GuiEvents(),
				new BlockClickHandler()
		);

		new WorkerHandler(this);

		// Обработка каждого тика
		new TickTimerHandler(this, Collections.singletonList(
				new FountainHandler(this)
		), clientSocket, playerDataManager).runTaskTimer(this, 0, 1);

		VisitorHandler.init(this, 1);
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

	public Collection<User> getUsers() {
		return playerDataManager.getUsers();
	}

	private void requestConfigurations() {
		try {
			RequestConfigurationsPackage pckg = clientSocket.writeAndAwaitResponse(new RequestConfigurationsPackage()).get(3L, TimeUnit.SECONDS);
			fillConfigurations(new ConfigurationsPackage(pckg.getConfigData(), pckg.getGuisData(), pckg.getItemsData()));
		} catch(Exception ex) {
			ex.printStackTrace();
			System.out.println("We can't receive museum configurations! Retry in 3sec");
			try {
				Thread.sleep(3000L);
			} catch(Exception ignored) {}
			requestConfigurations();
		}
	}

	private void fillConfigurations(ConfigurationsPackage pckg) {
		YamlConfiguration itemsConfig = YamlConfiguration.loadConfiguration(reader(pckg.getItemsData()));
		itemsConfig.getKeys(false)
				.forEach(key -> Lemonade.parse(itemsConfig.getConfigurationSection(key)).register(key));

		// Инициализация "умных" иконок в гуишках
		MuseumGuis.registerItemizers(this);

		// Загрузка всех инвентарей
		Guis.loadGuis(YamlConfiguration.loadConfiguration(reader(pckg.getGuisData())));

		this.configuration = YamlConfiguration.loadConfiguration(reader(pckg.getConfigData()));
	}

}
