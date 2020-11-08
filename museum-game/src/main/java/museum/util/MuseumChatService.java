package museum.util;

import lombok.val;
import museum.App;
import museum.packages.UserChatPackage;
import museum.player.User;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import ru.cristalix.core.IPlatform;
import ru.cristalix.core.IServerPlatform;
import ru.cristalix.core.chat.ChatContext;
import ru.cristalix.core.chat.ChatService;
import ru.cristalix.core.formatting.Formatting;
import ru.cristalix.core.permissions.IPermissionService;

import java.util.HashSet;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public final class MuseumChatService extends ChatService implements Listener {

	private final Server server;
	private final IPermissionService permissionService;

	public MuseumChatService(IPermissionService permissionService, Server server) {
		super(IPlatform.get(), permissionService);
		this.server = server;
		this.permissionService = permissionService;
	}

	@Override
	public void enable() {
		try {
			super.enable();
		} catch (Exception e) {
			e.printStackTrace();
		}
		val eventExecutor = IServerPlatform.get().getPlatformEventExecutor();
		eventExecutor.registerListener(AsyncPlayerChatEvent.class, this, event -> {
			event.setCancelled(true);
			val toRemove = event.getRecipients();
			val extended = new ru.cristalix.core.event.AsyncPlayerChatEvent(event.getPlayer(),
					TextComponent.fromLegacyText(event.getMessage()), new HashSet<>(toRemove));
			toRemove.clear();
			server.getPluginManager().callEvent(extended);
			if (extended.isCancelled()) {
				return;
			}
			val components = extended.getMessage();
			if (components == null)
				return;
			components.thenAccept(comp -> App.getApp().getClientSocket().write(new UserChatPackage(ComponentSerializer.toString(comp))));
		}, EventPriority.HIGH, true);
		eventExecutor.registerListener(ru.cristalix.core.event.AsyncPlayerChatEvent.class, this, event -> {
			val player = event.getPlayer();
			val uuid = player.getUniqueId();
			val chatView = getChatView(uuid);
			String error = null;
			if (chatView.isSilenced() && !player.hasPermission(SILENCE_BYPASS))
				error = "Ты сейчас не можешь писать в чат!";
			else if (!player.hasPermission(COOLDOWN_BYPASS) && chatView.isOnCooldown(uuid))
				error = "Погоди перед отправкой следующего сообщения";

			if (error != null) {
				player.sendMessage(Formatting.error(error));
				event.setCancelled(true);
				return;
			}

			String legacy = ChatColor.stripColor(TextComponent.toLegacyText(event.getOriginalMessage()));
			val global = legacy.charAt(0) == '!';
			legacy = global ? legacy.substring(1) : legacy;
			chatView.setOnCooldown(uuid);
			val original = event.getRecipients();
			val context = new ChatContext(legacy, global, original.stream()
					.map(Player::getUniqueId)
					.collect(Collectors.toSet())
			);
			val filtered = chatView.filter(uuid, context);
			original.clear();
			val iterator = filtered.iterator();
			for (int i = 0; i < filtered.size(); i++) {
				val ps = this.server.getPlayer(iterator.next());
				if (ps != null) original.add(ps);
			}
			val oldLegacy = legacy;
			User user = App.getApp().getUser(uuid);
			val builder = new ComponentBuilder(new TextComponent(""));
			if (user.getPrefix() != null) builder.append(new TextComponent(user.getPrefix() + "§8 ┃ "));

			event.setMessage(chatView.getFormattedComponent(uuid, context).thenCompose(message -> {
				builder.append(message);
				val future = permissionService.getNameColor(uuid);
				return future.thenApply(nameColor -> {
					if (nameColor != null) {
						builder.append("§8 ┃ §b" + user.getLevel())
								.append(TextComponent.fromLegacyText("§8" + ' ' + Formatting.ARROW_SYMBOL + ' '));
						return CompletableFuture.completedFuture(null);
					}
					return permissionService.getBestGroup(uuid).thenAccept(group -> {
						builder.append("§8 ┃ §b" + user.getLevel())
								.append(TextComponent.fromLegacyText("§8" + ' ' + Formatting.ARROW_SYMBOL + ' '));
					});
				});
			}).thenApply(future -> builder.append("§f" + oldLegacy)).thenApply(__ -> builder.create()));
		}, EventPriority.LOW, true);
		eventExecutor.registerListener(
				PlayerJoinEvent.class,
				this,
				event -> event.setJoinMessage(null),
				EventPriority.HIGH,
				true
		);
		eventExecutor.registerListener(
				PlayerQuitEvent.class,
				this,
				event -> {
					event.setQuitMessage(null);
					setChatView(event.getPlayer().getUniqueId(), null);
				},
				EventPriority.NORMAL, false
		);
	}

	@Override
	public void disable() throws Exception {
		super.disable();
		HandlerList.unregisterAll(this);
	}

}
