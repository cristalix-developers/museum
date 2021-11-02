package museum;

import lombok.Getter;
import museum.data.BoosterInfo;
import museum.packages.GlobalBoostersPackage;
import museum.packages.MuseumPackage;
import museum.socket.ServerSocketHandler;
import museum.utils.UtilTime;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.*;
import ru.cristalix.core.CoreApi;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Getter
public class BoosterManager implements Subservice {

	private static final HoverEvent HOVER_EVENT = new HoverEvent(HoverEvent.Action.SHOW_TEXT, new BaseComponent[] {
			new TextComponent("§eНАЖМИ НА МЕНЯ")
	});
	private static final ClickEvent CLICK_EVENT = new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/thx");
	private final List<BoosterInfo> globalBoosters = new ArrayList<>();
	private final Map<UUID, Set<UUID>> thanksMap = new ConcurrentHashMap<>();

	public BoosterManager() {
		this.updateOnRealms();

		CoreApi.get().getPlatform().getScheduler().runAsyncRepeating(this::tick, 15, TimeUnit.SECONDS);
		CoreApi.get().getPlatform().getScheduler().runAsyncRepeating(this::every4Minute, 4, TimeUnit.MINUTES);
	}

	@Override
	public MuseumPackage createPackage() {
		return new GlobalBoostersPackage(new ArrayList<>(globalBoosters));
	}

	private void tick() {
		List<BoosterInfo> mustDeleted = new ArrayList<>(1);
		globalBoosters.forEach(type -> {
			if (type.getUntil() < System.currentTimeMillis()) mustDeleted.add(type);
		});

		if (mustDeleted.isEmpty()) return;

		mustDeleted.forEach(booster -> {
			MuseumService.alert("§fБустер закончился!", "§b" + booster.getType().getName());
			MuseumService.alertMessage("§f§bi§f Глобальный бустер §b" + booster.getType().getName() + " §fзакончился!");
			globalBoosters.remove(booster);
			thanksMap.remove(booster.getUuid());
		});
		this.updateOnRealms();
	}

	private void every4Minute() {
		if (!globalBoosters.isEmpty()) {
			ComponentBuilder alertMessage = new ComponentBuilder("      \n").color(ChatColor.YELLOW);
			globalBoosters.forEach((type) ->
					alertMessage.bold(false)
							.append("Бустер ").color(ChatColor.WHITE)
							.append(type.getType().getName()).color(ChatColor.AQUA)
							.append(" от ").color(ChatColor.WHITE)
							.append(type.getOwnerName()).color(ChatColor.YELLOW)
							.append(" осталось ").color(ChatColor.WHITE)
							.append(UtilTime.formatTime(type.getUntil() - System.currentTimeMillis(), true)).color(ChatColor.GREEN)
							.append("\n")
			);
			alertMessage.append("[Клик] Поблагодарить ")
					.event(CLICK_EVENT)
					.event(HOVER_EVENT)
					.append("/thx")
					.event(CLICK_EVENT)
					.event(HOVER_EVENT)
					.color(ChatColor.LIGHT_PURPLE)
					.bold(true)
					.append("\n");
			alertMessage.append("        \n");
			MuseumService.alertMessage(alertMessage.create());
		}
		globalBoosters.forEach(booster -> {
			int thanksCount = thanksMap.computeIfAbsent(booster.getUuid(), (g) -> new HashSet<>()).size();
			MuseumService.sendMessage(Collections.singleton(booster.getOwner()), "§f§bi§f За время работы вашего бустера §b" + booster.getType().getName() + "§f вас поблагодарили §e" + thanksCount + " §fигроков!");
		});
	}

	public long executeThanks(UUID user) {
		return globalBoosters.stream()
				.filter(booster -> thanksMap.computeIfAbsent(booster.getUuid(), uuid -> new HashSet<>()).add(user))
				.peek(booster -> MuseumService.asyncExtra(
						booster.getOwner(),
						data -> data.getIncome() * MuseumService.INCOME_MULTIPLIER
				)).count();
	}

	public void push(BoosterInfo booster) {
		MuseumService.globalBoosters.save(booster);
		if (!booster.isGlobal())
			return;
		globalBoosters.add(booster);
		notifyBoosters();
		MuseumService.alert("§eБустер активирован!", "§b" + booster.getType().getName());
		MuseumService.alertMessage("§f§bi§f Игрок §e" + booster.getOwnerName() + "§f активировал глобальный бустер §b" + booster.getType().getName() + " §fна час! Поблагодарить его §d§l/thx");
	}

	public CompletableFuture<List<BoosterInfo>> receiveGlobal() {
		List<BoosterInfo> list = new ArrayList<>();
		CompletableFuture<List<BoosterInfo>> future = new CompletableFuture<>();
		MuseumService.globalBoosters.findAll().thenAccept(all -> list.addAll(all.values().stream()
				.filter(boosterInfo -> !boosterInfo.isGlobal() && boosterInfo.getUntil() > System.currentTimeMillis())
				.collect(Collectors.toList())
		));
		future.complete(list);
		return future;
	}

	public CompletableFuture<List<BoosterInfo>> receiveLocal(UUID user) {
		List<BoosterInfo> list = new ArrayList<>();
		CompletableFuture<List<BoosterInfo>> future = new CompletableFuture<>();
		MuseumService.globalBoosters.findAll().thenAccept(all -> list.addAll(all.values().stream()
				.filter(boosterInfo -> boosterInfo.getOwner().equals(user))
				.filter(boosterInfo -> !boosterInfo.isGlobal() && boosterInfo.getUntil() > System.currentTimeMillis())
				.collect(Collectors.toList())
		));
		future.complete(list);
		return future;
	}

	private void notifyBoosters() {
		ServerSocketHandler.broadcast(pckg());
	}

	public GlobalBoostersPackage pckg() {
		return new GlobalBoostersPackage(new ArrayList<>(globalBoosters));
	}
}
