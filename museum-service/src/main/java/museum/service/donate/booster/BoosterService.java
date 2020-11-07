package museum.service.donate.booster;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.mongodb.client.model.Filters;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import museum.data.BoosterInfo;
import museum.packages.ExtraDepositUserPackage;
import museum.packages.GlobalBoostersPackage;
import museum.packages.MuseumPackage;
import museum.packages.ThanksExecutePackage;
import museum.service.MuseumService;
import museum.service.conduct.IConductService;
import museum.service.data.MongoAdapter;
import museum.service.user.ServiceUser;
import ru.cristalix.core.CoreApi;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.SECONDS;

@Getter
@RequiredArgsConstructor
public class BoosterService implements IBoosterService {

	private final MuseumService museumService;
	private final IConductService conductService;

	private MongoAdapter<BoosterInfo> storageAdapter;

	private final Collection<BoosterInfo> globalBoosters = Collections.newSetFromMap(new ConcurrentHashMap<>());

	private final Multimap<BoosterInfo, UUID> thanksMap = HashMultimap.create();

	@Override
	public void enable() {

		storageAdapter = museumService.createStorageAdapter(BoosterInfo.class, "globalBoosters");

		storageAdapter.findAll(Filters.gt("until", System.currentTimeMillis()))
				.thenAccept(boosters -> {
					globalBoosters.addAll(boosters.values());
					conductService.broadcast(createBundle());
				});

		conductService.registerHandler(ThanksExecutePackage.class, (realm, museumPackage) -> {
			double boosters = executeThanks(museumPackage.getUser());
			userData.find(museumPackage.getUser()).thenAccept(data -> {
				extra(museumPackage.getUser(), data.getIncome() * INCOME_MULTIPLIER * boosters);
				museumPackage.setBoostersCount(boosters);
				send(channel, museumPackage);
			});
		});

		CoreApi.get().getPlatform().getScheduler().runAsyncRepeating(this::tick, 1, SECONDS);
		CoreApi.get().getPlatform().getScheduler().runAsyncRepeating(this::broadcastThxInfo, 4, MINUTES);
	}

	public MuseumPackage createBundle() {
		return new GlobalBoostersPackage(globalBoosters);
	}

	private void tick() {
		List<BoosterInfo> expired = globalBoosters.stream()
				.filter(BoosterInfo::isExpired)
				.collect(Collectors.toList());

		if (expired.isEmpty()) return;

		expired.forEach(booster -> {
			globalBoosters.remove(booster);
			thanksMap.removeAll(booster);
		});
		conductService.broadcast(createBundle());
	}

	private void broadcastThxInfo() {
		globalBoosters.forEach(booster -> {
			int thanksCount = thanksMap.get(booster).size();
			MuseumService.sendMessage(Collections.singleton(booster.getOwner()),
					"§f[§c!§f] За время работы вашего бустера §b" + booster.getType().getName() + "§f вас поблагодарили §e" + thanksCount + " §fигроков!");
		});
	}

	public double executeThanks(UUID uuid) {
		ServiceUser user = museumService.getUserService().getUser(uuid);

		double bonusMoney = globalBoosters.stream()
				.filter(booster -> thanksMap.put(booster, uuid))
				.mapToDouble(booster -> user.getInfo().getIncome() * MuseumService.INCOME_MULTIPLIER)
				.sum();

		user.getRealm().send(new ExtraDepositUserPackage(uuid, bonusMoney));
		return bonusMoney;
	}

	@Override
	public void addGlobalBooster(BoosterInfo booster) {
		if (!booster.isGlobal())
			return;
		globalBoosters.add(booster);
		storageAdapter.save(booster);
		conductService.broadcast(createBundle());
	}

}
