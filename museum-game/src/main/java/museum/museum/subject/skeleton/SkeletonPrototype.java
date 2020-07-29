package museum.museum.subject.skeleton;

import lombok.Getter;
import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftArmorStand;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import museum.App;
import museum.prototype.Prototype;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Обеспечивает одинаковое расположение костей при каждом перезапуске
 */
@Getter
public class SkeletonPrototype implements Prototype {

	private final String title;
	private final int size;
	private final int piecesAmount;
	private final String address;
	private final Rarity rarity;
	private final List<Piece> pieces;
	private final List<Fragment> fragments;

	public SkeletonPrototype(String title, int size, int piecesAmount, Rarity rarity, @NonNull String address, Location worldOrigin) {
		this.title = title;
		this.size = size;
		this.piecesAmount = piecesAmount;
		this.address = address;
		this.rarity = rarity;
		Map<ArmorStand, Location> allStands = new HashMap<>();
		for (Entity entity : App.getApp().getMap().getWorld().getEntities()) {
			if (entity.getType() != EntityType.ARMOR_STAND)
				continue;
			allStands.put((ArmorStand) entity, entity.getLocation());
		}

		List<ArmorStand> stands = new ArrayList<>();
		recursiveTree(allStands, stands);

		this.pieces = stands.stream()
				.map(as -> new Piece(((CraftArmorStand) as).getHandle(), worldOrigin))
				.collect(Collectors.toList());

		this.fragments = pieces.stream()
				.collect(Collectors.groupingBy(Piece::getName))
				.entrySet().stream()
				.map(e -> new Fragment(address, e.getKey(), e.getValue(), e.getValue().stream().mapToInt(p -> p.getHandle().getId()).toArray()))
				.collect(Collectors.toList());
	}

	public static void recursiveTree(Map<ArmorStand, Location> selection, List<ArmorStand> walked) {
		List<ArmorStand> current = new ArrayList<>();
		for (Map.Entry<ArmorStand, Location> e : selection.entrySet()) {
			if (walked.contains(e.getKey())) continue;
			for (Map.Entry<ArmorStand, Location> e1 : selection.entrySet()) {
				if (e1.getValue().distanceSquared(e.getValue()) < 16)
					current.add(e1.getKey());
			}
		}
		walked.addAll(current);
		if (!current.isEmpty()) recursiveTree(selection, walked);
	}

	public String getTitle() {
		return this.title;
	}

}
