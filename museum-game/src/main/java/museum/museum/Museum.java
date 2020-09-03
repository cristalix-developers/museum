package museum.museum;

import clepto.bukkit.Lemonade;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_12_R1.CraftWorld;
import ru.cristalix.core.scoreboard.IScoreboardService;
import museum.App;
import museum.data.MuseumInfo;
import museum.museum.collector.CollectorNavigator;
import museum.museum.map.MuseumPrototype;
import museum.museum.map.SubjectType;
import museum.museum.subject.Allocation;
import museum.museum.subject.MarkerSubject;
import museum.museum.subject.Subject;
import museum.player.User;
import museum.prototype.Storable;
import museum.util.LocationTree;
import museum.util.warp.WarpUtil;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * @author func 22.05.2020
 */
@Setter
@Getter
public class Museum extends Storable<MuseumInfo, MuseumPrototype> {

	private final CraftWorld world;
	private double income;
    private String title;

    public Museum(MuseumPrototype prototype, MuseumInfo info, User owner) {
		super(prototype, info, owner);
		this.world = App.getApp().getWorld();
		this.title = info.title;

		List<MarkerSubject> allMarkers = getSubjects(SubjectType.MARKER);

		this.getSubjects(SubjectType.COLLECTOR).forEach(collector -> {
			List<MarkerSubject> markers = allMarkers.stream()
					.filter(marker -> marker.getCollectorId() == collector.getId())
					.collect(Collectors.toList());
			List<MarkerSubject> route = LocationTree.order(markers, MarkerSubject::getLocation);
			collector.setNavigator(new CollectorNavigator(prototype, world,
					route.stream().map(MarkerSubject::getLocation).collect(Collectors.toList())));
		});
	}

    @Override
    public void updateInfo() {
        cachedInfo.title = title;
    }

    public void show(User user) {
		new WarpUtil.WarpBuilder(prototype.getAddress()).build().warp(user);

		cachedInfo.views++;

		StringBuilder builder = new StringBuilder();
		for (Subject subject : user.getSubjects()) {
			if (!subject.isAllocated()) continue;
			if (builder.length() > 0) builder.append('|');
			builder.append(subject.getAllocation().getClientData())
					.append('_').append(subject.getPrototype().getTitle())
					.append('_').append(subject.getPrototype().getPrice());
		}
		String payload = builder.toString();
		System.out.println("Sent " + payload + " to " + user.getName());
		user.sendPayload("museumsubjects", payload);

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

	public void hide(User user) {

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
			if (s.getPrototype().getType() == type) list.add((T) s);
		});
		return list;
	}

	public void processClick(User user, Subject subject) {
		user.performCommand("gui manipulator");
	}

	public long getViews() {
        return cachedInfo.getViews();
    }

    public Date getCreationDate() {
        return cachedInfo.getCreationDate();
    }

    public void incrementViews() {
        cachedInfo.views++;
    }

}
