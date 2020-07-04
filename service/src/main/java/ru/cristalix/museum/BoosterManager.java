package ru.cristalix.museum;

import ru.cristalix.core.CoreApi;
import ru.cristalix.museum.boosters.BoosterType;
import ru.cristalix.museum.data.BoosterInfo;
import ru.cristalix.museum.packages.GlobalBoostersPackage;
import ru.cristalix.museum.packages.MuseumPackage;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

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

}
