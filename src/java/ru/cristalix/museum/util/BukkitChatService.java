package ru.cristalix.museum.util;

import lombok.val;
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
import ru.cristalix.museum.App;
import ru.cristalix.museum.packages.UserChatPackage;

import java.util.HashSet;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public final class BukkitChatService extends ChatService implements Listener {

	private final Server server;
	private final IPermissionService permissionService;

	public BukkitChatService(IPermissionService permissionService, Server server) {
		super(IPlatform.get(), permissionService);
		this.server = server;
		this.permissionService = permissionService;
	}

	@Override
	public void enable() throws Exception {
		super.enable();
		val eventExecutor = IServerPlatform.get().getPlatformEventExecutor();
		eventExecutor.registerListener(AsyncPlayerChatEvent.class,
				this,
				e -> {
					e.setCancelled(true);
					val toRemove = e.getRecipients();
					val extended = new ru.cristalix.core.event.AsyncPlayerChatEvent(e.getPlayer(),
							TextComponent.fromLegacyText(e.getMessage()), new HashSet<>(toRemove));
					toRemove.clear();
					server.getPluginManager().callEvent(extended);
					if (extended.isCancelled()) {
						return;
					}
					val components = extended.getMessage();
					if (components == null) return;
					components.thenAccept(comp -> {
						App.getApp().getClientSocket().write(new UserChatPackage(ComponentSerializer.toString(comp)));
					});
				}, EventPriority.HIGH, true);
		eventExecutor.registerListener(ru.cristalix.core.event.AsyncPlayerChatEvent.class,
				this,
				e -> {
					val p = e.getPlayer();
					val uuid = p.getUniqueId();
					val chatView = getChatView(uuid);
					if (chatView.isSilenced() && !p.hasPermission(SILENCE_BYPASS)) {
						p.sendMessage(Formatting.error("Ты сейчас не можешь писать в чат!"));
						e.setCancelled(true);
						return;
					}
					if (!p.hasPermission(COOLDOWN_BYPASS) && chatView.isOnCooldown(uuid)) {
						p.sendMessage(Formatting.error("Погоди перед отправкой следующего сообщения"));
						e.setCancelled(true);
						return;
					}
					String legacy = ChatColor.stripColor(TextComponent.toLegacyText(e.getOriginalMessage()));
					val global = legacy.charAt(0) == '!';
					if (global) legacy = legacy.substring(1);
					chatView.setOnCooldown(uuid);
					val original = e.getRecipients();
					val recipients = original.stream().map(Player::getUniqueId).collect(Collectors.toSet());
					val context = new ChatContext(legacy, global, recipients);
					val filtered = chatView.filter(uuid, context);
					original.clear();
					val size = filtered.size();
					val iterator = filtered.iterator();
					val server = this.server;
					for (int i = 0; i < size; i++) {
						val ps = server.getPlayer(iterator.next());
						if (ps != null) original.add(ps);
					}
					val _legacy = legacy;
					val builder = new ComponentBuilder(new TextComponent(""));
					e.setMessage(chatView.getFormattedComponent(uuid, context).thenCompose(message -> {
						builder.append(message);
						val future = permissionService.getNameColor(uuid);
						return future.thenApply(nameColor -> {
							if (nameColor != null) {
								builder.append(TextComponent.fromLegacyText(nameColor + ' ' + Formatting.ARROW_SYMBOL + ' '));
								return CompletableFuture.completedFuture(null);
							}
							return permissionService.getBestGroup(uuid).thenAccept(group -> {
								builder.append(TextComponent.fromLegacyText(group.getNameColor() + ' ' + Formatting.ARROW_SYMBOL + ' '));
							});
						});
					}).thenApply(__ -> {
						val future = chatView.format(uuid, _legacy);
						return future.thenAccept(builder::append);
					}).thenApply(__ -> builder.create()));
				}, EventPriority.LOW, true);
		eventExecutor.registerListener(PlayerJoinEvent.class,
				this,
				e -> e.setJoinMessage(null),
				EventPriority.HIGH, true);
		eventExecutor.registerListener(PlayerQuitEvent.class,
				this,
				e -> {
					e.setQuitMessage(null);
					setChatView(e.getPlayer().getUniqueId(), null);
				},
				EventPriority.NORMAL, false);
	}

	@Override
	public void disable() throws Exception {
		super.disable();
		HandlerList.unregisterAll(this);
	}

}
