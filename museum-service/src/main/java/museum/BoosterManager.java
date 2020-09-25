package museum;

import lombok.Getter;
import museum.boosters.BoosterType;
import museum.data.BoosterInfo;
import museum.packages.GlobalBoostersPackage;
import museum.packages.MuseumPackage;
import museum.socket.ServerSocketHandler;
import museum.utils.UtilTime;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ComponentBuilder;
import ru.cristalix.core.CoreApi;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Getter
public class BoosterManager implements Subservice {

	private final Map<BoosterType, BoosterInfo> globalBoosters;
	private Map<UUID, Set<UUID>> thanksMap = new ConcurrentHashMap<>();

	public BoosterManager() {
		try {
			globalBoosters = MuseumService.globalBoosters
					.findAll().get().values().stream()
					.collect(Collectors.toMap(BoosterInfo::getType, b -> b, (var0, var1) -> var0, ConcurrentHashMap::new));
		} catch (InterruptedException | ExecutionException e) {
			throw new RuntimeException(e);
		}

		this.updateOnRealms();

		CoreApi.get().getPlatform().getScheduler().runAsyncRepeating(this::tick, 15, TimeUnit.SECONDS);
		CoreApi.get().getPlatform().getScheduler().runAsyncRepeating(this::every4Minute, 4, TimeUnit.MINUTES);
	}

	@Override
	public MuseumPackage createPackage() {
		return new GlobalBoostersPackage(new ArrayList<>(globalBoosters.values()));
	}

	private void tick() {
		List<BoosterInfo> mustDeleted = new ArrayList<>(1);
		globalBoosters.forEach((type, boost) -> {
			if (boost.getUntil() < System.currentTimeMillis()) mustDeleted.add(boost);
		});

		if (mustDeleted.isEmpty()) return;

		mustDeleted.forEach(booster -> {
			MuseumService.alert("§eБустер закончился!", "§b" + booster.getType().getName());
			MuseumService.alertMessage("§f[§c!§f] Глобальный бустер §b" + booster.getType().getName() + " §fзакончился!");
			globalBoosters.remove(booster.getType());
			thanksMap.remove(booster.getUuid());
		});
		this.updateOnRealms();
	}

	private void every4Minute() {
		if (!globalBoosters.isEmpty()) {
			ComponentBuilder alertMessage = new ComponentBuilder("================\n").color(ChatColor.YELLOW);
			alertMessage.append("     \n");
			globalBoosters.forEach((type, boost) ->
					alertMessage
							.append("Бустер ").color(ChatColor.WHITE)
							.append(type.getName()).color(ChatColor.AQUA)
							.append(" от ").color(ChatColor.WHITE)
							.append(boost.getOwnerName()).color(ChatColor.YELLOW)
							.append(" | ").bold(true).color(ChatColor.BLUE)
							.append(UtilTime.formatTime(boost.getUntil() - System.currentTimeMillis(), true)).color(ChatColor.GREEN)
							.append("\n")
			);
			alertMessage.append("        \n");
			alertMessage.append("Поблагодарить ").append("/thx").color(ChatColor.LIGHT_PURPLE).bold(true).append("\n");
			alertMessage.append("        \n");
			alertMessage.append("================\n").color(ChatColor.YELLOW);
			MuseumService.alertMessage(alertMessage.create());
		}
		globalBoosters.values().forEach(booster -> {
			int thanksCount = thanksMap.computeIfAbsent(booster.getUuid(), (g) -> new HashSet<>()).size();
			MuseumService.sendMessage(Collections.singleton(booster.getOwner()), "§f[§c!§f] За время работы вашего бустера §b" + booster.getType().getName() + "§f вас поблагодарили §e" + thanksCount + " §fигроков!");
		});
	}

	public long executeThanks(UUID user) {
		return globalBoosters.values()
				.stream()
				.filter(booster -> thanksMap.computeIfAbsent(booster.getUuid(), uuid -> new HashSet<>()).add(user))
				.peek(booster -> MuseumService.extra(
						booster.getOwner(),
						null,
						(double) MuseumService.THANKS_SECONDS)
				).count();
	}

	public void push(BoosterInfo booster) {
		MuseumService.globalBoosters.save(booster);
		if (!booster.isGlobal())
			return;
		globalBoosters.put(booster.getType(), booster);
		notifyBoosters();
		MuseumService.alert("§eБустер активирован!", "§b" + booster.getType().getName());
		MuseumService.alertMessage("§f[§c!§f] Игрок §e" + booster.getOwnerName() + "§f активировал глобальный бустер §b" + booster.getType().getName() + " §fна час! Поблагодарить его §d§l/thx");
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
		return new GlobalBoostersPackage(new ArrayList<>(globalBoosters.values()));
	}
}
