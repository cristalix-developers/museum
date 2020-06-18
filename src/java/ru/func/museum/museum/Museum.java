package ru.func.museum.museum;

import clepto.bukkit.Lemonade;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Delegate;
import org.bukkit.Location;
import org.bukkit.Material;
import ru.cristalix.core.scoreboard.IScoreboardService;
import ru.func.museum.App;
import ru.func.museum.data.MuseumInfo;
import ru.func.museum.element.Element;
import ru.func.museum.excavation.Excavation;
import ru.func.museum.museum.coin.Coin;
import ru.func.museum.museum.collector.Collector;
import ru.func.museum.museum.hall.Hall;
import ru.func.museum.museum.hall.template.space.Space;
import ru.func.museum.player.User;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * @author func 22.05.2020
 * @project Museum
 */
@Setter
@Getter
public class Museum {

	private final User owner;

	@Delegate
	private final MuseumInfo info;

	private final List<Collector> collectors;
	private final List<Space> spaces = new ArrayList<>();

	private double income;

	public Museum(MuseumInfo info, User owner) {
		this.info = info;
		this.owner = owner;
		this.collectors = info.getCollectorInfos().stream().map(Collector::new).collect(Collectors.toList());
	}

	public void load(App plugin, User user) {
		info.views++;

		updateIncrease();
		user.setBreakLess(-2);
		user.sendAnime();

		IScoreboardService.get().setCurrentObjective(user.getUuid(), "main");

		user.setCurrentMuseum(this);

		user.setCoins(Collections.newSetFromMap(new ConcurrentHashMap<>()));

		user.getInventory().remove(Material.SADDLE);

		if (!user.getMuseums().contains(this)) {
			user.getInventory().setItem(8, Lemonade.get("back").render());
		}

		user.teleport(new Location(Excavation.WORLD, spawnX, spawnY, spawnZ));
		// Поготовка заллов
		spaces.forEach(space -> space.show(user));
		collectors.forEach(collector -> collector.show(user));
	}

	public void unload(App app, User user) {
		// Очстка витрин, коллекторов
		spaces.forEach(space -> space.hide(user));
		collectors.forEach(collector -> collector.hide(user));

		// Очистка монет
		Set<Coin> coins = app.getArchaeologistMap().get(guest.getUniqueId()).getCoins();
		coins.forEach(coin -> coin.remove(connection));
		coins.clear();
	}

	public void updateIncrease() {
		income = .1;
		for (Space space : spaces)
			income += space.getIncome();
	}

}
