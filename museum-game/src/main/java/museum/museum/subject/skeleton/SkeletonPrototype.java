package museum.museum.subject.skeleton;

import lombok.Getter;
import museum.prototype.Prototype;
import net.minecraft.server.v1_12_R1.Packet;
import net.minecraft.server.v1_12_R1.PacketListenerPlayOut;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftArmorStand;
import org.bukkit.entity.ArmorStand;

import java.util.*;

import static java.util.stream.Collectors.groupingBy;
import static museum.museum.subject.skeleton.Displayable.orientedOffset;

@Getter
public class SkeletonPrototype implements Prototype, Displayable {

	private final String title;
	private final int size;
	private final String address;
	private final Rarity rarity;
	private final int price;
	private final Map<Fragment, V4> fragmentOffsetMap = new HashMap<>();

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
	public void getShowPackets(Collection<Packet<PacketListenerPlayOut>> buffer, V4 position) {
		fragmentOffsetMap.forEach((piece, offset) -> piece.getShowPackets(buffer, orientedOffset(position, offset)));
	}

	@Override
	public void getUpdatePackets(Collection<Packet<PacketListenerPlayOut>> buffer, V4 position) {
		fragmentOffsetMap.forEach((piece, offset) -> piece.getUpdatePackets(buffer, orientedOffset(position, offset)));
	}

	@Override
	public void getHidePackets(Collection<Packet<PacketListenerPlayOut>> buffer) {
		fragmentOffsetMap.keySet().forEach(piece -> piece.getHidePackets(buffer));
	}

	public V4 getOffset(Fragment fragment) {
		return this.fragmentOffsetMap.get(fragment);
	}

}
