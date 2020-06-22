package ru.cristalix.museum.museum;

import clepto.bukkit.Lemonade;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Delegate;
import org.bukkit.Material;
import ru.cristalix.core.scoreboard.IScoreboardService;
import ru.cristalix.museum.Storable;
import ru.cristalix.museum.data.MuseumInfo;
import ru.cristalix.museum.museum.map.MuseumPrototype;
import ru.cristalix.museum.museum.map.SubjectPrototype;
import ru.cristalix.museum.museum.subject.Subject;
import ru.cristalix.museum.player.User;
import ru.cristalix.museum.prototype.Managers;

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
public class Museum implements Storable<MuseumInfo> {

	private final User owner;
	private final MuseumPrototype prototype;

	@Delegate
	private final MuseumInfo info;

	private final List<Subject> subjects;

	private double income;

	public Museum(MuseumInfo info, User owner) {
		this.info = info;
		this.owner = owner;

		this.prototype = Managers.museum.getPrototype(info.getAddress());

		this.subjects = info.getSubjectInfos().stream()
				.map(subjectInfo -> {
					SubjectPrototype prototype = Managers.subject.getPrototype(subjectInfo.getPrototypeAddress());
					return prototype.getType().provide(this, subjectInfo, prototype);
				}).collect(Collectors.toList());
	}

	@Override
	public MuseumInfo generateInfo() {
		this.info.subjectInfos = Storable.store(subjects);
		return info;
	}

	public void load(User user) {
		info.views++;

		updateIncrease();
		user.sendAnime();

		IScoreboardService.get().setCurrentObjective(user.getUuid(), "main");

		user.setCurrentMuseum(this);

		user.setCoins(Collections.newSetFromMap(new ConcurrentHashMap<>()));

		user.getPlayer().getInventory().remove(Material.SADDLE);

		if (this.owner != user) {
			user.getPlayer().getInventory().setItem(8, Lemonade.get("back").render());
		}

		user.getPlayer().teleport(prototype.getSpawnPoint());
		subjects.forEach(space -> space.show(user));
	}

	public void unload(User user) {
		subjects.forEach(space -> space.hide(user));

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
