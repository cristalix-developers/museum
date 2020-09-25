package museum.museum.subject.skeleton;

import lombok.Getter;
import museum.prototype.Prototype;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftArmorStand;
import org.bukkit.entity.ArmorStand;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.groupingBy;

@Getter
public class SkeletonPrototype implements Prototype, Piece {

	private final String title;
	private final int size;
	private final String address;
	private final Rarity rarity;
	private final int price;
	private final Map<Fragment, V4> childrenMap = new HashMap<>();

	public SkeletonPrototype(String address, String title, Location worldOrigin, int size, Rarity rarity, List<ArmorStand> stands, int price) {
		this.title = title;
		this.size = size;
		this.address = address;
		this.rarity = rarity;
		this.price = price;

		stands.stream()
				.collect(groupingBy(as -> as.getCustomName() == null ? "???" : as.getCustomName()))
				.forEach((fragmentAddress, fragmentStands) -> {
					double x = 0, y = 0, z = 0;
					for (ArmorStand stand : fragmentStands) {
						Location loc = stand.getLocation();
						x += loc.x;
						y += loc.y;
						z += loc.z;
					}
					int amount = fragmentStands.size();
					V4 fragmentOffset = new V4(x / amount, y / amount, z / amount, 0);
					Fragment fragment = new Fragment(fragmentAddress);

					for (ArmorStand stand : fragmentStands) {
						AtomPiece piece = new AtomPiece(((CraftArmorStand) stand).getHandle());
						V4 pieceOffset = V4.fromLocation(stand.getLocation().subtract(fragmentOffset.toVector()));
						pieceOffset.setRot(stand.getLocation().getYaw());
						fragment.getChildrenMap().put(piece, pieceOffset);
					}
					this.childrenMap.put(fragment, V4.fromVector(fragmentOffset.toVector().subtract(worldOrigin.toVector())));
				});
	}

	public Collection<Fragment> getFragments() {
		return childrenMap.keySet();
	}

	public V4 getOffset(Fragment fragment) {
		return this.childrenMap.get(fragment);
	}

}
