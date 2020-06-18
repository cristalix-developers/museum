package ru.func.museum.museum;

import clepto.bukkit.Lemonade;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Delegate;
import org.bukkit.Material;
import ru.cristalix.core.scoreboard.IScoreboardService;
import ru.func.museum.App;
import ru.func.museum.data.MuseumInfo;
import ru.func.museum.museum.coin.Coin;
import ru.func.museum.museum.collector.Collector;
import ru.func.museum.museum.hall.template.space.Subject;
import ru.func.museum.museum.map.MuseumPrototype;
import ru.func.museum.player.User;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * @author func 22.05.2020
 */
@Setter
@Getter
public class Museum {

	private final User owner;
	private final MuseumPrototype prototype;

	@Delegate
	private final MuseumInfo info;

	private final List<Collector> collectors;
	private final List<Subject> subjects = new ArrayList<>();

	private double income;

	public Museum(MuseumInfo info, User owner) {
		this.info = info;
		this.owner = owner;
		this.prototype = App.getApp().getMuseumMap().getPrototype(info.getAddress());
		this.collectors = info.getCollectorInfos().stream()
				.map(collectorInfo -> new Collector(this, collectorInfo))
				.collect(Collectors.toList());
	}

	public void load(App plugin, User user) {
		info.views++;

		updateIncrease();
		user.setBreakLess(-2);
		user.sendAnime();

		IScoreboardService.get().setCurrentObjective(user.getUuid(), "main");

		user.setCurrentMuseum(this);

		user.setCoins(Collections.newSetFromMap(new ConcurrentHashMap<>()));

		user.getPlayer().getInventory().remove(Material.SADDLE);

		if (!user.getMuseums().contains(this)) {
			user.getPlayer().getInventory().setItem(8, Lemonade.get("back").render());
		}

		user.getPlayer().teleport(prototype.getSpawnPoint());
		// Поготовка заллов
		subjects.forEach(space -> space.show(user));
		collectors.forEach(collector -> collector.show(user));
	}

	public void unload(User user) {
		// Очстка витрин, коллекторов
		subjects.forEach(space -> space.hide(user));
		collectors.forEach(collector -> collector.hide(user));

		// Очистка монет
		Set<Coin> coins = user.getCoins();
		coins.forEach(coin -> coin.remove(user.getConnection()));
		coins.clear();
	}

	public void updateIncrease() {
		income = .1;
		for (Subject subject : subjects)
			income += subject.getIncome();
	}

}
