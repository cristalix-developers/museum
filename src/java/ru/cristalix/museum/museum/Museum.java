package ru.cristalix.museum.museum;

import clepto.bukkit.B;
import clepto.bukkit.Lemonade;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Delegate;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_12_R1.CraftWorld;
import ru.cristalix.core.scoreboard.IScoreboardService;
import ru.cristalix.museum.App;
import ru.cristalix.museum.Storable;
import ru.cristalix.museum.data.MuseumInfo;
import ru.cristalix.museum.museum.collector.CollectorNavigator;
import ru.cristalix.museum.museum.map.MuseumPrototype;
import ru.cristalix.museum.museum.map.SubjectType;
import ru.cristalix.museum.museum.subject.Allocation;
import ru.cristalix.museum.museum.subject.MarkerSubject;
import ru.cristalix.museum.museum.subject.Subject;
import ru.cristalix.museum.player.User;
import ru.cristalix.museum.prototype.Managers;
import ru.cristalix.museum.util.LocationTree;
import ru.cristalix.museum.util.WarpUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * @author func 22.05.2020
 */
@Setter
@Getter
public class Museum implements Storable<MuseumInfo> {

	private final User owner;
	private final MuseumPrototype prototype;
	private final CraftWorld world;

	@Delegate
	private final MuseumInfo info;

	private double income;

	public Museum(MuseumInfo info, User owner) {
		this.info = info;
		this.owner = owner;
		this.world = App.getApp().getWorld();

		this.prototype = Managers.museum.getPrototype(info.getAddress());

		getSubjects(SubjectType.COLLECTOR).forEach(collector -> {
			List<MarkerSubject> markers = getSubjects(SubjectType.MARKER).stream()
					.filter(marker -> marker.getCollectorId() == collector.getId())
					.collect(Collectors.toList());
			List<MarkerSubject> route = LocationTree.order(markers, MarkerSubject::getLocation);
			collector.setNavigator(new CollectorNavigator(prototype, world,
					route.stream().map(MarkerSubject::getLocation).collect(Collectors.toList())));
		});
	}

	@Override
	public MuseumInfo generateInfo() {
		return info;
	}

	public void load(User user) {
		new WarpUtil.WarpBuilder(prototype.getAddress()).build().warp(user);

		info.views++;

		user.sendAnime();

		IScoreboardService.get().setCurrentObjective(user.getUuid(), "main");

		user.setCoins(Collections.newSetFromMap(new ConcurrentHashMap<>()));
		user.setCurrentMuseum(this);

		user.getPlayer().getInventory().remove(Material.SADDLE);

		if (this.owner != user) {
			user.getPlayer().getInventory().setItem(8, Lemonade.get("back").render());
		}
		Bukkit.getScheduler().runTaskLaterAsynchronously(App.getApp(), () ->
				iterateSubjects(s -> s.show(user)), 20L);

		updateIncrease();
	}

	public void unload(User user) {
		iterateSubjects(s -> s.hide(user, false));

		Set<Coin> coins = user.getCoins();
		coins.forEach(coin -> coin.remove(user.getConnection()));
		coins.clear();
	}

	private void iterateSubjects(Consumer<Subject> action) {
		for (Subject subject : owner.getSubjects()) {
			Allocation allocation = subject.getAllocation();
//			if (allocation == null) continue;
//			if (!prototype.getBox().contains(allocation.getOrigin())) continue;
			action.accept(subject);
		}
	}

	public void updateIncrease() {
		double[] i = {.1};
		iterateSubjects(s -> i[0] += s.getIncome()); // Completely safe and professional code.
		income = i[0];
	}
	public List<Subject> getSubjects() {
		List<Subject> list = new ArrayList<>();
		iterateSubjects(list::add);
		return list;
	}

	@SuppressWarnings ("unchecked")
	public <T extends Subject> List<T> getSubjects(SubjectType<T> type) {
		List<T> list = new ArrayList<>();
		iterateSubjects(s -> {
			if (s.getType() == type) list.add((T) s);
		});
		return list;
	}

	public void processClick(User user, int x, int y, int z) {
		B.run(() -> B.bc("§aClick at " + x + " " + y + " " + z));
		for (Subject subject : getSubjects()) {
			if (subject.getAllocation() == null) continue;
			for (Location loc : subject.getAllocation().getAllocatedBlocks()) {
				if (loc.getBlockX() == x && loc.getBlockY() == y && loc.getBlockZ() == z) {
					subject.hide(user, true);
				}
			}
		}
	}

}
