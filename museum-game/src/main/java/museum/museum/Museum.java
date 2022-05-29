package museum.museum;

import clepto.bukkit.B;
import clepto.bukkit.item.Items;
import lombok.Getter;
import lombok.Setter;
import lombok.val;
import me.func.mod.Anime;
import me.func.mod.conversation.ModTransfer;
import museum.App;
import museum.cosmos.boer.Boer;
import museum.data.MuseumInfo;
import museum.fragment.Fragment;
import museum.museum.collector.CollectorNavigator;
import museum.museum.map.MuseumPrototype;
import museum.museum.map.SubjectType;
import museum.museum.subject.Allocation;
import museum.museum.subject.CollectorSubject;
import museum.museum.subject.MarkerSubject;
import museum.museum.subject.Subject;
import museum.player.State;
import museum.player.User;
import museum.player.prepare.BeforePacketHandler;
import museum.player.prepare.PreparePlayerBrain;
import museum.prototype.Storable;
import museum.util.ChunkWriter;
import museum.util.LocationUtil;
import museum.util.SubjectLogoUtil;
import net.minecraft.server.v1_12_R1.BlockPosition;
import net.minecraft.server.v1_12_R1.Chunk;
import net.minecraft.server.v1_12_R1.IBlockData;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_12_R1.CraftWorld;
import org.bukkit.inventory.ItemStack;
import ru.cristalix.core.math.V3;
import ru.cristalix.core.util.UtilV3;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static museum.museum.subject.Allocation.Action.*;

/**
 * @author func 22.05.2020
 */
@Setter
@Getter
public class Museum extends Storable<MuseumInfo, MuseumPrototype> implements State {

	private final ItemStack menu = Items.render("menu").asBukkitMirror();
	private final ItemStack backItem = Items.render("back").asBukkitMirror();
	private final ItemStack visitorMenu = Items.render("visitor-menu").asBukkitMirror();
	private final ItemStack placeMenu = Items.render("place-menu").asBukkitMirror();

	private final CraftWorld world;
	private double income;
	private String title;
	private Set<Coin> coins = ConcurrentHashMap.newKeySet();

	public Museum(MuseumPrototype prototype, MuseumInfo info, User owner) {
		super(prototype, info, owner);
		this.world = App.getApp().getWorld();
		this.title = info.title;

		for (Subject subject : owner.getSubjects()) {
			V3 position = subject.getCachedInfo().getLocation();
			if (position == null) continue;
			Location location = UtilV3.toLocation(position, App.getApp().getWorld());
			if (!this.getPrototype().getBox().contains(location)) continue;
			B.run(() -> this.addSubject(subject, location));
		}

		List<MarkerSubject> allMarkers = owner.getSubjects().stream()
				.filter(subject -> subject.getPrototype().getType() == SubjectType.MARKER)
				.map(MarkerSubject.class::cast)
				.collect(Collectors.toList());

		owner.getSubjects().stream()
				.filter(subject -> subject.getPrototype().getType() == SubjectType.COLLECTOR)
				.map(CollectorSubject.class::cast)
				.forEach(collector -> {
					List<MarkerSubject> markers = allMarkers.stream()
							.filter(marker -> marker.getCollectorId() == collector.getId() && marker.getLocation() != null)
							.collect(Collectors.toList());
					List<MarkerSubject> route = LocationUtil.orderTree(markers, MarkerSubject::getLocation);
					if (route != null) collector.setNavigator(new CollectorNavigator(prototype, world,
							route.stream().map(MarkerSubject::getLocation).collect(Collectors.toList())));
				});

		updateIncrease();
	}

	@Override
	public void updateInfo() {
		cachedInfo.title = title;
	}

	@Override
	public void enterState(User user) {
		teleportUser(user);

		val player = user.getPlayer();
		val inventory = player.getInventory();

		if (owner.getExperience() >= PreparePlayerBrain.EXPERIENCE)
			giveMenu(user);

		if (this.owner != user) {
			inventory.setItem(8, backItem);
			cachedInfo.views++;
		} else {
			int collectorAmount = 0;

			for (Subject subject : owner.getSubjects()) {
				if (subject instanceof CollectorSubject && subject.isAllocated())
					collectorAmount++;
				if (collectorAmount > 2) {
					B.postpone(30, () -> Anime.itemTitle(owner.handle(), BeforePacketHandler.EMERGENCY_STOP, "Снятие коллекторов", "ОШИБКА", 3.0));
					for (Subject collector : owner.getSubjects())
						if (collector instanceof CollectorSubject)
							collector.setAllocation(null);
					continue;
				}
				if (!subject.isAllocated() && !subject.getPrototype().getType().equals(SubjectType.MARKER))
					inventory.addItem(SubjectLogoUtil.encodeSubjectToItemStack(subject));
			}
			for (Fragment relic : user.getRelics().values()) {
				if (!(relic instanceof Boer))
					inventory.addItem(relic.getItem());
			}
		}

		B.postpone(1, () -> {
			if (user.getGrabbedArmorstand() == null)
				player.setAllowFlight(true);
		});
		B.postpone(20, () -> {
			for (Subject subject : getSubjects()) {
				subject.getAllocation().perform(user, UPDATE_BLOCKS, SPAWN_PIECES, SPAWN_DISPLAYABLE);
			}

			new ModTransfer()
					.json(user.getSubjects().stream()
							.filter(Subject::isAllocated)
							.map(Subject::getDataForClient)
							.filter(Objects::nonNull)
							.collect(Collectors.toList())
					).send("museumsubjects", user.handle());
		});
	}

	public void giveMenu(User user) {
		val inventory = user.getInventory();
		user.getPlayer().setItemOnCursor(null);
		user.getPlayer().getOpenInventory().getTopInventory().clear();
		inventory.clear();
		inventory.setItem(0, menu);
		inventory.setItem(3, visitorMenu);
		inventory.setItem(5, placeMenu);
	}

	@Override
	public void leaveState(User user) {
		this.iterateSubjects(subject -> subject.getAllocation().perform(user, HIDE_BLOCKS, HIDE_PIECES, DESTROY_DISPLAYABLE));
		user.setLastLocation(user.getLocation());
		user.setLastPosition(UtilV3.fromVector(user.getLocation().toVector()));

		Coin.bulkRemove(user.getConnection(), coins);
		coins.clear();
	}

	@Override
	public boolean playerVisible() {
		return false;
	}

	@Override
	public boolean nightVision() {
		return true;
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

	public long getViews() {
		return cachedInfo.getViews();
	}

	public Date getCreationDate() {
		return cachedInfo.getCreationDate();
	}

	public void incrementViews() {
		cachedInfo.views++;
	}

	@Override
	public void rewriteChunk(User user, ChunkWriter chunkWriter) {
		Chunk chunk = chunkWriter.getChunk();
		iterateSubjects(subject -> {
			Allocation allocation = subject.getAllocation();
			for (Map.Entry<BlockPosition, IBlockData> entry : allocation.getBlocks().entrySet()) {
				BlockPosition position = entry.getKey();
				if (position.getX() >> 4 == chunk.locX && position.getZ() >> 4 == chunk.locZ)
					chunkWriter.write(position, entry.getValue());
			}

			allocation.perform(Collections.singleton(user), chunk, Allocation.Action.SPAWN_PIECES);
		});
	}

	public boolean addSubject(Subject subject, Location location) {
		Allocation allocation = Allocation.allocate(this, subject.getCachedInfo().getColor(), subject.getPrototype(), location);
		subject.setAllocation(allocation);
		return allocation != null;
	}

	private void iterateSubjects(Consumer<Subject> action) {
		for (Subject subject : owner.getSubjects()) {
			Allocation allocation = subject.getAllocation();
			if (allocation == null) continue;
			if (!prototype.getBox().contains(allocation.getOrigin())) continue;
			action.accept(subject);
		}
	}

	private void teleportUser(User user) {
		user.teleport(prototype.getBox().contains(user.getLastLocation()) && owner == user ?
				user.getLastLocation() :
				prototype.getSpawn()
		);
	}
}