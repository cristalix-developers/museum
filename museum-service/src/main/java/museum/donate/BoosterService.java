package museum.donate;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import museum.MongoAdapter;
import museum.MuseumService;
import museum.boosters.BoosterType;
import museum.data.BoosterInfo;
import museum.packages.GlobalBoostersPackage;
import museum.packages.MuseumPackage;
import museum.socket.ServerSocketHandler;
import museum.utils.UtilTime;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.*;
import ru.cristalix.core.CoreApi;
import ru.cristalix.core.IService;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Getter
@RequiredArgsConstructor
public class BoosterService implements IBoosterService {

	private static final HoverEvent HOVER_EVENT = new HoverEvent(HoverEvent.Action.SHOW_TEXT, new BaseComponent[] {
			new TextComponent("§eНАЖМИ НА МЕНЯ")
	});
	private static final ClickEvent CLICK_EVENT = new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/thx");

	private final MuseumService museumService;

	private MongoAdapter<BoosterInfo> storageAdapter;

	private Collection<BoosterInfo> globalBoosters = Collections.newSetFromMap(new ConcurrentHashMap<>());
	private final Map<BoosterInfo, Set<UUID>> thanksMap = new ConcurrentHashMap<>();

	@Override
	public void enable() {

		storageAdapter = museumService.createStorageAdapter(BoosterInfo.class, "globalBoosters");

		storageAdapter.findAll().thenAccept(boosters -> {
			globalBoosters.addAll(boosters.values());
			ServerSocketHandler.broadcast(createPackage());
		});

		CoreApi.get().getPlatform().getScheduler().runAsyncRepeating(this::tick, 1, TimeUnit.SECONDS);
	}

	public MuseumPackage createPackage() {
		return new GlobalBoostersPackage(new ArrayList<>(globalBoosters.values()));
	}

	private void tick() {
		List<BoosterInfo> mustDeleted = new ArrayList<>(1);
		globalBoosters.forEach(booster -> {
			if (booster.isExpired()) mustDeleted.add(booster);
		});

		if (mustDeleted.isEmpty()) return;

		mustDeleted.forEach(booster -> {
			globalBoosters.remove(booster);
			thanksMap.remove(booster.getUuid());
		});
		ServerSocketHandler.broadcast(createPackage());
	}

	private void every4Minute() {
		if (!globalBoosters.isEmpty()) {
			ComponentBuilder alertMessage = new ComponentBuilder("================\n").color(ChatColor.YELLOW);
			alertMessage.append("     \n");
			globalBoosters.forEach((type, boost) ->
					alertMessage.bold(false)
							.append("Бустер ").color(ChatColor.WHITE)
							.append(type.getName()).color(ChatColor.AQUA)
							.append(" от ").color(ChatColor.WHITE)
							.append(boost.getOwnerName()).color(ChatColor.YELLOW)
							.append(" | ").bold(true).color(ChatColor.BLUE)
							.append(UtilTime.formatTime(boost.getUntil() - System.currentTimeMillis(), true)).color(ChatColor.GREEN)
							.append("\n")
			);
			alertMessage.append("        \n");
			alertMessage.append("Поблагодарить ")
					.event(CLICK_EVENT)
					.event(HOVER_EVENT)
					.append("/thx")
					.event(CLICK_EVENT)
					.event(HOVER_EVENT)
					.color(ChatColor.LIGHT_PURPLE)
					.bold(true)
					.append("\n");
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
		return globalBoosters
				.stream()
				.filter(booster -> thanksMap.computeIfAbsent(booster.getUuid(), uuid -> new HashSet<>()).add(user))
				.peek(booster -> MuseumService.asyncExtra(
						booster.getOwner(),
						data -> data.getIncome() * MuseumService.INCOME_MULTIPLIER
				)).count();
	}

	public void addGlobalBooster(BoosterInfo booster) {
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
