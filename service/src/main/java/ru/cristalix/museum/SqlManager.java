package ru.cristalix.museum;

import lombok.Getter;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ComponentBuilder;
import ru.cristalix.core.CoreApi;
import ru.cristalix.museum.boosters.Booster;
import ru.cristalix.museum.boosters.BoosterType;
import ru.cristalix.museum.packages.GlobalBoostersPackage;
import ru.cristalix.museum.socket.ServerSocketHandler;
import ru.cristalix.museum.utils.UtilTime;
import ru.ilyafx.sql.BaseSQL;
import ru.ilyafx.sql.QueryResult;
import ru.ilyafx.sql.api.queries.FactoryManager;
import ru.ilyafx.sql.api.queries.factories.AsyncQueryFactory;
import ru.ilyafx.sql.api.queries.factories.SyncQueryFactory;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class SqlManager {

	private final BaseSQL sql;
	private final SyncQueryFactory sync;
	private final AsyncQueryFactory async;

	@Getter
	private Map<BoosterType, Booster> globalBoosters = new ConcurrentHashMap<>();

	private Map<UUID, Set<UUID>> thanksMap = new ConcurrentHashMap<>();

	public SqlManager(BaseSQL sql) {
		this.sql = sql;
		FactoryManager manager = new FactoryManager(sql);
		this.sync = manager.sync();
		this.async = manager.async();
		sync.unsafeUpdate("CREATE TABLE IF NOT EXISTS `boosters`" +
				" (`id` varchar(36) NOT NULL," +
				" `user` varchar(36) NOT NULL," +
				" `username` varchar(36) NOT NULL," +
				" `booster` varchar(50) NOT NULL," +
				" `until` LONG NOT NULL," +
				" `time` LONG NOT NULL," +
				" `multiplier` DOUBLE NOT NULL," +
				" `global` BOOLEAN NOT NULL," +
				" PRIMARY KEY(`id`)) ENGINE=InnoDB DEFAULT CHARSET=utf8;");
		receiveGlobal().thenAccept(list -> {
			globalBoosters = list.stream().collect(Collectors.toMap(Booster::getType, booster -> booster));
			notifyBoosters();
		});
		CoreApi.get().getPlatform().getScheduler().runAsyncRepeating(() -> {
			if (!globalBoosters.isEmpty()) {
				ComponentBuilder alertMessage = new ComponentBuilder("================\n").color(ChatColor.YELLOW);
				alertMessage.append("     \n");
				globalBoosters.forEach((type, boost) ->
						alertMessage
								.append("Бустер ").color(ChatColor.WHITE)
								.append(type.getName()).color(ChatColor.AQUA)
								.append(" от ").color(ChatColor.WHITE)
								.append(boost.getUserName()).color(ChatColor.YELLOW)
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
				int thanksCount = thanksMap.computeIfAbsent(booster.getUniqueId(), (g) -> new HashSet<>()).size();
				MuseumService.sendMessage(Collections.singleton(booster.getUser()), "§f[§c!§f] За время работы вашего бустера §b" + booster.getType().getName() + "§f вас поблагодарили §e" + thanksCount + " §fигроков!");
			});
		}, 4L, TimeUnit.MINUTES);
		CoreApi.get().getPlatform().getScheduler().runAsyncRepeating(() -> {
			List<Booster> mustDeleted = new ArrayList<>(1);
			globalBoosters.forEach((type, boost) -> {
				if (boost.getUntil() < System.currentTimeMillis()) mustDeleted.add(boost);
			});
			if (!mustDeleted.isEmpty()) {
				mustDeleted.forEach(booster -> {
					MuseumService.alert("§eБустер закончился!", "§b" + booster.getType().getName());
					MuseumService.alertMessage("§f[§c!§f] Глобальный бустер §b" + booster.getType().getName() + " §fзакончился!");
					globalBoosters.remove(booster.getType());
					thanksMap.remove(booster.getUniqueId());
				});
				notifyBoosters();
			}
		}, 15L, TimeUnit.SECONDS);
	}

	public int executeThanks(UUID user) {
		return (int) globalBoosters.values().stream().filter(booster -> {
			return thanksMap.computeIfAbsent(booster.getUniqueId(), (g) -> new HashSet<>()).add(user);
		}).peek(booster -> MuseumService.extra(booster.getUser(), null, (double) MuseumService.THANKS_SECONDS)).count();
	}

	public void push(Booster booster) {
		async.prepareUpdate("INSERT INTO boosters VALUES (?,?,?,?,?,?,?);", ps -> {
			try {
				ps.setString(1, booster.getUniqueId().toString());
				ps.setString(2, booster.getUser().toString());
				ps.setString(3, booster.getUserName());
				ps.setString(4, booster.getType().name());
				ps.setLong(5, booster.getUntil());
				ps.setLong(6, booster.getTime());
				ps.setDouble(7, booster.getMultiplier());
				ps.setBoolean(8, booster.isGlobal());
			} catch (Exception ignored) {
			}
		});
		if (booster.isGlobal()) {
			globalBoosters.put(booster.getType(), booster);
			notifyBoosters();
			MuseumService.alert("§eБустер активирован!", "§b" + booster.getType().getName());
			MuseumService.alertMessage("§f[§c!§f] Игрок §e" + booster.getUserName() + "§f активировал глобальный бустер §b" + booster.getType().getName() + " §fна час! Поблагодарить его §d§l/thx");
		}
	}

	public CompletableFuture<List<Booster>> receiveGlobal() {
		return async.prepareGet("SELECT * FROM boosters WHERE global = 1 AND until > ?", ps -> {
			try {
				ps.setLong(1, System.currentTimeMillis());
			} catch (Exception ignored) {
			}
		}).thenApply(res -> new ArrayList<>(res.all().stream().map(this::fromSection).collect(Collectors.toList())));
	}

	public CompletableFuture<List<Booster>> receiveLocal(UUID user) {
		return async.prepareGet("SELECT * FROM boosters WHERE user = ? AND global = 0 AND until > ?", ps -> {
			try {
				ps.setString(1, user.toString());
				ps.setLong(2, System.currentTimeMillis());
			} catch (Exception ignored) {
			}
		}).thenApply(res -> res.all().stream().map(this::fromSection).collect(Collectors.toList()));
	}

	private Booster fromSection(QueryResult.SQLSection sec) {
		UUID uniqueId = UUID.fromString(sec.lookupValue("id"));
		UUID user = UUID.fromString(sec.lookupValue("user"));
		String userName = sec.lookupValue("username");
		BoosterType type = BoosterType.valueOf(sec.lookupValue("booster"));
		long until = Long.parseLong(sec.lookupValue("until"));
		long time = Long.parseLong(sec.lookupValue("time"));
		double multiplier = sec.lookupValue("multiplier");
		boolean global = sec.lookupValue("global");
		return new Booster(uniqueId, user, userName, type, until, time, multiplier, global);
	}

	private void notifyBoosters() {
		ServerSocketHandler.broadcast(pckg());
	}

	public GlobalBoostersPackage pckg() {
		return new GlobalBoostersPackage(new ArrayList<>(globalBoosters.values()));
	}

}
