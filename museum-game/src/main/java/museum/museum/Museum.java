package museum.museum;

import clepto.bukkit.B;
import clepto.bukkit.Lemonade;
import lombok.Getter;
import lombok.Setter;
import lombok.val;
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
import museum.util.MessageUtil;
import museum.util.SubjectLogoUtil;
import museum.util.warp.Warp;
import museum.util.warp.WarpUtil;
import org.bukkit.craftbukkit.v1_12_R1.CraftWorld;
import org.bukkit.inventory.ItemStack;
import ru.cristalix.core.scoreboard.IScoreboardService;

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

	private final ItemStack menu = Lemonade.get("menu").render();

	private Warp warp;
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
		warp = new WarpUtil.WarpBuilder(prototype.getAddress()).build();
	}

	@Override
	public void updateInfo() {
		cachedInfo.title = title;
	}

	public void show(User user) {
		if (!Objects.equals(user.getLastWarp(), warp))
			warp.warp(user);

		// Если игрок после раскопок, то убрать ее, иначе если он не зашел впервые, то зачем прогружать?
		if (user.getExcavation() != null) {
			user.setExcavation(null);
		} else if (user.getCurrentMuseum() != null)
			return;

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
		user.sendPayload("museumsubjects", payload);

		user.sendAnime();

		IScoreboardService.get().setCurrentObjective(user.getUuid(), "main");

		user.setCoins(ConcurrentHashMap.newKeySet());
		user.setCurrentMuseum(this);

		val player = user.getPlayer();
		val inventory = player.getInventory();
		inventory.clear();
		inventory.setItem(0, menu);

		if (this.owner != user) {
			player.getInventory().setItem(8, Lemonade.get("back").render());
		}
		B.postpone(20, () -> iterateSubjects(subject -> {
			subject.show(user);
			if (user == owner) {
				val allocation = subject.getAllocation();
				if (allocation == null)
					player.getInventory().addItem(SubjectLogoUtil.encodeSubjectToItemStack(subject));
			}
		}));

		updateIncrease();
	}

	public void hide(User user) {
		iterateSubjects(s -> s.hide(user));

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
		iterateSubjects(s -> i[0] += s.getIncome());
		income = i[0];
	}

	public List<Subject> getSubjects() {
		List<Subject> list = new ArrayList<>();
		iterateSubjects(list::add);
		return list;
	}

	public Subject getSubjectByUuid(UUID uuid) {
		for (val subject : getSubjects())
			if (subject.getCachedInfo().getUuid().equals(uuid))
				return subject;
		return null;
	}

	@SuppressWarnings("unchecked")
	public <T extends Subject> List<T> getSubjects(SubjectType<T> type) {
		List<T> list = new ArrayList<>();
		iterateSubjects(s -> {
			if (s.getPrototype().getType() == type) list.add((T) s);
		});
		return list;
	}

	public void processClick(User user, Subject subject) {
		if (user.getMuseums().stream().noneMatch(museum -> user.getCurrentMuseum().equals(museum))) {
			MessageUtil.find("non-root").send(user);
			return;
		}
		user.performCommand("gui manipulator " + subject.getCachedInfo().getUuid().toString());
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
