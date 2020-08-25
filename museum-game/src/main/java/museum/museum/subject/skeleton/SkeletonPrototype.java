package museum.museum.subject.skeleton;

import lombok.Getter;
import museum.prototype.Prototype;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftArmorStand;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.groupingBy;
import static museum.museum.subject.skeleton.Displayable.orientedOffset;

@Getter
public class SkeletonPrototype implements Prototype, Displayable {

	private final String title;
	private final int size;
	private final String address;
	private final Rarity rarity;
	private final Map<Fragment, V4> fragmentOffsetMap = new HashMap<>();

	public SkeletonPrototype(String address, String title, Location worldOrigin, int size, Rarity rarity, List<ArmorStand> stands) {
		this.title = title;
		this.size = size;
		this.address = address;
		this.rarity = rarity;

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
						Piece piece = new Piece(((CraftArmorStand) stand).getHandle());
						V4 pieceOffset = V4.fromLocation(stand.getLocation().subtract(fragmentOffset.toVector()));
						pieceOffset.setRot(stand.getLocation().getYaw());
						fragment.getPieceOffsetMap().put(piece, pieceOffset);
					}

					this.fragmentOffsetMap.put(fragment, V4.fromVector(fragmentOffset.toVector().subtract(worldOrigin.toVector())));

				});

	}

	public Collection<Fragment> getFragments() {
		return fragmentOffsetMap.keySet();
	}

	@Override
	public void show(Player player, V4 position) {
		fragmentOffsetMap.forEach((fragment, offset) -> fragment.show(player, orientedOffset(position, offset)));
	}

	@Override
	public void update(Player player, V4 position) {
		fragmentOffsetMap.forEach((fragment, offset) -> fragment.show(player, orientedOffset(position, offset)));
	}

	@Override
	public void hide(Player player) {
		for (Fragment fragment : this.getFragments())
			fragment.hide(player);
	}

	public V4 getOffset(Fragment fragment) {
		return this.fragmentOffsetMap.get(fragment);
	}

}
