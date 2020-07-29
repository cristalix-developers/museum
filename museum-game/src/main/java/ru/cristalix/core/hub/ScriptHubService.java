package ru.cristalix.core.hub;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.LoadingCache;
import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.val;
import museum.App;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import ru.cristalix.core.GlobalSerializers;
import ru.cristalix.core.IServerPlatform;
import ru.cristalix.core.IService;
import ru.cristalix.core.Task;
import ru.cristalix.core.dependencies.ServiceDependencies;
import ru.cristalix.core.display.IDisplayService;
import ru.cristalix.core.display.messages.JavaScriptMessage;
import ru.cristalix.core.nbt.NbtBase;
import ru.cristalix.core.nbt.NbtGsonAdapter;
import ru.cristalix.core.network.Capability;
import ru.cristalix.core.network.ISocketClient;
import ru.cristalix.core.network.packages.HubItemsPackage;
import ru.cristalix.core.network.packages.HubItemsUpdatePackage;
import ru.cristalix.core.permissions.IGroup;
import ru.cristalix.core.permissions.IPermissionService;
import ru.cristalix.core.realm.IRealmService;
import ru.cristalix.core.realm.RealmId;
import ru.cristalix.core.transfer.ITransferService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

import static io.netty.buffer.Unpooled.wrappedBuffer;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static ru.cristalix.core.GlobalSerializers.toJson;
import static ru.cristalix.core.hub.ScriptHubUtils.sendPayload;
import static ru.cristalix.core.permissions.StaffGroups.*;

@ServiceDependencies (@ServiceDependencies.Dependency (value = {IRealmService.class, ITransferService.class, IDisplayService.class, IPermissionService.class},
		required = true, description = {
		"IRealmService: required to check if any minigame of type is online or not",
		"ITransferService: to transfer players between minigames",
		"IPermissionService: to check if player can join on full server"
}))
@RequiredArgsConstructor
public final class ScriptHubService implements IService, Listener {

	public static final HubMenuSecondary
			TECHNICAL_MENU = HubMenuSecondary.virtual("TECHNICAL", Material.COMMAND, 0, "Всякие тестовые штуки"),
			BUILD_MENU = HubMenuSecondary.virtual("BUIL", Material.LIME_GLAZED_TERRACOTTA, 0, "Строительные сервера");

	private static final JavaScriptMessage CLIENT_CODE = new JavaScriptMessage(new String[] {
			"// ToDo: " // ToDo.
	});

	private final IServerPlatform serverPlatform;
	private final ISocketClient socketClient;
	private final IRealmService realmService;
	private final IDisplayService displayService;
	private final IPermissionService permissionService;

	private final LoadingCache<HubMenu, byte[]> serverDataCache = CacheBuilder.newBuilder()
			.expireAfterAccess(500, MILLISECONDS)
			.build(new FunctionalCacheLoader<>(hubMenu -> toJson(hubMenu.reloadInfo()).getBytes()));

	private final Map<Player, HubMenu> player2MenuMap = new HashMap<>();

	@Getter
	private HubMenuMain mainMenu;

	private final Capability capability = Capability.builder().className(HubItemsUpdatePackage.class.getName()).notification(true).build();

	private final BiConsumer<RealmId, HubItemsPackage> packetListenerA = (__, packet) -> generateMainMenu(packet.getItems());
	private final BiConsumer<RealmId, HubItemsUpdatePackage> packetListenerB = (__, packet) -> generateMainMenu(packet.getItems());
	private Task<?> updateTask;

	@Override
	public void enable() throws Exception {

		socketClient.registerCapability(capability);
		socketClient.addListener(HubItemsPackage.class, packetListenerA);
		socketClient.addListener(HubItemsUpdatePackage.class, packetListenerB);

		Bukkit.getMessenger().registerIncomingPluginChannel(App.getApp(), "hubgui", this::handleClientCommand);
		GlobalSerializers.configure(builder -> builder.registerTypeHierarchyAdapter(NbtBase.class, new NbtGsonAdapter()));

		val events = serverPlatform.getPlatformEventExecutor();

		events.registerListener(PlayerQuitEvent.class, this, event -> {
			this.player2MenuMap.remove(event.getPlayer());
		}, EventPriority.LOW, false);

		events.registerListener(PlayerJoinEvent.class, this, e -> {
			this.serverPlatform.getScheduler().runSyncDelayed(() -> {
				this.setup(e.getPlayer());
			}, 250, MILLISECONDS);
		}, EventPriority.LOW, false);

		if (this.mainMenu == null) {
			val packet = this.socketClient.<HubItemsPackage>writeAndAwaitResponse(new HubItemsPackage()).get(3, SECONDS);
			generateMainMenu(packet.getItems());
		}

		this.updateTask = serverPlatform.getScheduler().runAsyncRepeating(() -> {
			player2MenuMap.forEach(this::sendUpdate);
		}, 300, MILLISECONDS);
	}

	@Override
	public void disable() {
		socketClient.removeListener(HubItemsPackage.class, packetListenerA);
		socketClient.removeListener(HubItemsUpdatePackage.class, packetListenerB);
		socketClient.removeCapability(capability);
		HandlerList.unregisterAll(this);
		updateTask.cancel();
		updateTask = null;
	}

	private void handleClientCommand(String channel, Player player, byte[] data) {
		HubMenu menu = player2MenuMap.get(player);
		if (menu == null) return;
		menu.handleClick(this, player, new String(data));
	}

	public void openMenu(Player player, HubMenu menu) {
		String menuId;
		if (menu == null) {
			menuId = "CLOSE";
			this.player2MenuMap.remove(player);
		} else {
			menuId = menu.getRealmType();
			this.player2MenuMap.put(player, menu);
			this.sendUpdate(player, menu);
		}
		sendPayload(player, "hubguiaction", menuId);
	}

	public void sendUpdate(Player player, HubMenu menu) {
		sendPayload(player, "hubguiupdate", wrappedBuffer(serverDataCache.getUnchecked(menu)));
	}

	private void generateMainMenu(List<HubItem> items) {

		for (Player player : this.player2MenuMap.keySet())
			openMenu(player, null);
		this.player2MenuMap.clear();
		this.serverDataCache.invalidateAll();

		List<HubMenuSecondary> children = new ArrayList<>();
		children.add(TECHNICAL_MENU);
		children.add(BUILD_MENU);
		for (HubItem item : items)
			children.add(new HubMenuSecondary(item));

		this.mainMenu = new HubMenuMain(children);

	}

	protected void setup(Player player) {

		this.displayService.sendScripts(player.getUniqueId(), CLIENT_CODE);
		ByteBuf buffer = wrappedBuffer(mainMenu.getSetupData());
		sendPayload(player, "hubguisetup", buffer);

		boolean hasExtraSlots = HubUtil.areExtraSlotsAvailable(permissionService, player.getUniqueId());
		IGroup group = permissionService.getPermissionContextDirect(player.getUniqueId()).getStaffGroup();
		boolean isBuilder = group == BUILDER || group == SR_BUILDER || group == CUR_BUILDER;
		boolean isAdmin = group.getPriority() >= ADMIN.getPriority();

		sendPayload(player, "hubguiperms", toJson(new boolean[] {hasExtraSlots, isBuilder, isAdmin}));

	}

}