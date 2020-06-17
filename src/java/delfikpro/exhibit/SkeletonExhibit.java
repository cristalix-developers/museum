package delfikpro.exhibit;

import clepto.ListUtils;
import clepto.cristalix.WorldMeta;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Обеспечивает одинаковое расположение костей при каждом перезапуске
 */
@Getter
public class SkeletonExhibit extends Exhibit {

	private final int piecesAmount;
	private final String address;
	private final List<Piece> pieces;
	private final List<Fragment> fragments = new ArrayList<>();

	public SkeletonExhibit(String title, int piecesAmount, @NonNull String address, WorldMeta worldMeta) {
		super(title);
		this.piecesAmount = piecesAmount;
		this.address = address;
		Location worldOrigin = worldMeta.getLocation(address);
		Map<ArmorStand, Location> allStands = new HashMap<>();
		for (Entity entity : worldMeta.getWorld().getEntities()) {
			if (entity.getType() != EntityType.ARMOR_STAND)
				continue;
			allStands.put((ArmorStand) entity, entity.getLocation());
		}

		List<ArmorStand> stands = new ArrayList<>();
		recursiveTree(allStands, stands);

		this.pieces = stands.stream()
				.map(as -> new Piece(as, worldOrigin))
				.collect(Collectors.toList());

		Piece root = pieces.stream()
				.min(Comparator.comparingDouble(Piece::getDistanceSq))
				.orElseThrow(RuntimeException::new);

		List<Piece> free = new ArrayList<>(pieces);
		free.remove(root);
		List<Piece> linked = ListUtils.newArrayList(root);

		//noinspection StatementWithEmptyBody
		while (createLink(free, linked));

		List<Piece> tree = root.getAllChildren();

		double step = (double) tree.size() / piecesAmount;
		double currentBone = 0;
		for (int id = 0; id < piecesAmount; id++, currentBone += step) {
			int fromBone = (int) currentBone;
			int toBone = (int) (currentBone + step);
			if (toBone >= tree.size()) toBone = tree.size() - 1;
			fragments.add(new Fragment(address, id, tree.subList(fromBone, toBone)));
		}

	}


	private boolean createLink(List<Piece> free, List<Piece> linked) {
		double smallest = Double.MAX_VALUE;
		Piece nearest = null;
		Piece actualRoot = null;
		for (Piece root : linked) {
			for (Piece stand : free) {
				double length = stand.ditanceSquared(root);
				if (length > smallest)
					continue;
				smallest = length;
				nearest = stand;
				actualRoot = root;
			}
		}

		if (nearest == null) return false;
		actualRoot.link(nearest);
		free.remove(nearest);
		linked.add(nearest);
		return true;

	}

	private void recursiveTree(Map<ArmorStand, Location> selection, List<ArmorStand> walked) {
		List<ArmorStand> current = new ArrayList<>();
		for (Map.Entry<ArmorStand, Location> e : selection.entrySet()) {
			if (walked.contains(e.getKey())) continue;
			for (Map.Entry<ArmorStand, Location> e1 : selection.entrySet()) {
				if (e1.getValue().distanceSquared(e.getValue()) < 16) current.add(e1.getKey());
			}
		}
		walked.addAll(current);
		if (!current.isEmpty()) recursiveTree(selection, walked);
	}

}
