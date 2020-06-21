package ru.cristalix.museum;

import net.md_5.bungee.api.chat.ComponentBuilder;
import ru.cristalix.core.CoreApi;
import ru.cristalix.museum.boosters.Booster;
import ru.cristalix.museum.boosters.BoosterType;
import ru.cristalix.museum.packages.GlobalBoostersPackage;
import ru.cristalix.museum.socket.ServerSocketHandler;
import ru.ilyafx.sql.BaseSQL;
import ru.ilyafx.sql.QueryResult;
import ru.ilyafx.sql.api.queries.FactoryManager;
import ru.ilyafx.sql.api.queries.factories.AsyncQueryFactory;
import ru.ilyafx.sql.api.queries.factories.SyncQueryFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class SqlManager {

	private final BaseSQL sql;
	private final SyncQueryFactory sync;
	private final AsyncQueryFactory async;

	private Map<BoosterType, Booster> globalBoosters = new ConcurrentHashMap<>();

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
				" `multiplier` DOUBLE NOT NULL," +
				" `global` BOOLEAN NOT NULL," +
				" PRIMARY KEY(`id`)) ENGINE=InnoDB DEFAULT CHARSET=utf8;");
		receiveGlobal().thenAccept(list -> {
			globalBoosters = list.stream().collect(Collectors.toMap(Booster::getType, booster -> booster));
			notifyBoosters();
		});
		CoreApi.get().getPlatform().getScheduler().runAsyncRepeating(() -> {
			List<BoosterType> mustDeleted = new ArrayList<>(1);
			globalBoosters.forEach((type, boost) -> {
				if (boost.getUntil() < System.currentTimeMillis()) mustDeleted.add(type);
			});
			if (!mustDeleted.isEmpty()) {
				mustDeleted.forEach(type -> {
					MuseumService.alert("§eБустер закончился!", "§b" + type.getName());
					MuseumService.alertMessage("§f[§c!§f] Глобальный бустер §b" + type.getName() + " §fзакончился!");
					globalBoosters.remove(type);
				});
				notifyBoosters();
			}
		}, 15L, TimeUnit.SECONDS);
	}

	public void push(Booster booster) {
		async.prepareUpdate("INSERT INTO boosters VALUES (?,?,?,?,?,?);", ps -> {
			try {
				ps.setString(1, booster.getUniqueId().toString());
				ps.setString(2, booster.getUser().toString());
				ps.setString(3, booster.getUserName());
				ps.setString(4, booster.getType().name());
				ps.setLong(5, booster.getUntil());
				ps.setDouble(6, booster.getMultiplier());
				ps.setBoolean(7, booster.isGlobal());
			} catch (Exception ignored) {
			}
		});
		if (booster.isGlobal()) {
			globalBoosters.put(booster.getType(), booster);
			notifyBoosters();
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
		double multiplier = sec.lookupValue("multiplier");
		boolean global = sec.lookupValue("global");
		return new Booster(uniqueId, user, userName, type, until, multiplier, global);
	}

	private void notifyBoosters() {
		ServerSocketHandler.broadcast(pckg());
	}

	public GlobalBoostersPackage pckg() {
		return new GlobalBoostersPackage(new ArrayList<>(globalBoosters.values()));
	}

}
