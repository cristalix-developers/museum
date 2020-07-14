package ru.cristalix.museum;

import clepto.bukkit.B;
import clepto.bukkit.Lemonade;
import clepto.bukkit.gui.GuiEvents;
import clepto.bukkit.gui.Guis;
import clepto.cristalix.Cristalix;
import clepto.cristalix.mapservice.WorldMeta;
import lombok.Getter;
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
import ru.cristalix.core.map.BukkitWorldLoader;
import ru.cristalix.core.map.MapListDataItem;
import ru.cristalix.core.permissions.IPermissionService;
import ru.cristalix.core.realm.IRealmService;
import ru.cristalix.core.scoreboard.IScoreboardService;
import ru.cristalix.core.scoreboard.ScoreboardService;
import ru.cristalix.museum.client.ClientSocket;
import ru.cristalix.museum.command.MuseumCommand;
import ru.cristalix.museum.donate.DonateType;
import ru.cristalix.museum.gui.MuseumGuis;
import ru.cristalix.museum.listener.MuseumEventHandler;
import ru.cristalix.museum.listener.PassiveEventBlocker;
import ru.cristalix.museum.museum.map.SubjectType;
import ru.cristalix.museum.museum.subject.skeleton.SkeletonPrototype;
import ru.cristalix.museum.packages.*;
import ru.cristalix.museum.player.PlayerDataManager;
import ru.cristalix.museum.player.User;
import ru.cristalix.museum.prototype.Managers;
import ru.cristalix.museum.util.MuseumChatService;
import ru.cristalix.museum.visitor.VisitorManager;

import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.util.Base64;
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

		// Загрузка карты с сервера BUIL-1
		MapListDataItem mapInfo = Cristalix.mapService().getMapByGameTypeAndMapName("MODELS", "Dino")
				.orElseThrow(() -> new RuntimeException("Map museum/main wasn't found in the MapService"));

		// todo: temp commands
		B.regCommand((sender, args) -> {
			if (args.length == 0) return "§cИспользование: §e/money [Количество денег]";
			getUser(sender).setMoney(Double.parseDouble(args[0]));
			return "§aВаше количество денег изменено.";
		}, "money");

		B.regCommand((sender, args) -> {
			if (args.length == 0) return "§cИспользование: §e/dino [Динозавр]";
			SkeletonPrototype proto = Managers.skeleton.getPrototype(args[0]);
			if (proto == null) return "§cПрототип динозавра §e" + args[0] + "§c не найден.";
			getUser(sender).getSkeletons().get(proto).getUnlockedFragments().addAll(proto.getFragments());
			return "";
		}, "dino");

		try {
			this.map = new WorldMeta(Cristalix.mapService().loadMap(mapInfo.getLatest(), BukkitWorldLoader.INSTANCE).get());
		} catch (InterruptedException | ExecutionException e) {
			throw new RuntimeException(e);
		}
		this.map.getWorld().setGameRuleValue("mobGriefing", "false");

		// Загрузга всех построек (витрины/коллекторы), мэнеджеров
		SubjectType.init();
		Managers.init();

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
		this.playerDataManager = new PlayerDataManager(this);

		// Регистрация Core сервисов
		CoreApi.get().unregisterService(IChatService.class);
		CoreApi.get().registerService(IChatService.class, new MuseumChatService(IPermissionService.get(), getServer()));
		CoreApi.get().registerService(IScoreboardService.class, new ScoreboardService());
		CoreApi.get().registerService(IInventoryService.class, new InventoryService());

		// Регистрация обработчика пакета конфига
		clientSocket.registerHandler(ConfigurationsPackage.class, pckg -> {
			YamlConfiguration itemsConfig = YamlConfiguration.loadConfiguration(reader(pckg.getItemsData()));
			itemsConfig.getKeys(false)
					.forEach(key -> Lemonade.parse(itemsConfig.getConfigurationSection(key)).register(key));
			// Загрузка всех инвентарей
			Guis.loadGuis(YamlConfiguration.loadConfiguration(reader(pckg.getGuisData())));

			this.configuration = YamlConfiguration.loadConfiguration(reader(pckg.getConfigData()));
		});

		// Создание посетителей
		VisitorManager visitorManager = new VisitorManager(100, 10);

		// Инициализация промежуточных команд / Инвентарей
		new MuseumGuis(this);
		Bukkit.getPluginCommand("museum").setExecutor(new MuseumCommand(this));

		// Регистрация обработчиков событий
		B.events(
				playerDataManager,
				new PassiveEventBlocker(),
				new MuseumEventHandler(this),
				new GuiEvents()
		);

		// Обработка каждого тика
		new TickTimerHandler(this, visitorManager, clientSocket, playerDataManager)
				.runTaskTimer(this, 0, 1);
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
