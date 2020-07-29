package museum.museum.subject;

import clepto.cristalix.mapservice.Box;
import lombok.Data;
import net.minecraft.server.v1_12_R1.*;
import org.bukkit.Location;
import org.bukkit.Material;
import ru.cristalix.core.math.V3;
import ru.cristalix.core.util.UtilV3;
import museum.App;
import museum.data.SubjectInfo;
import museum.museum.map.SubjectPrototype;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static clepto.bukkit.B.nms;

@Data
public class Allocation {

	private final Location origin;
	private final Collection<PacketPlayOutBlockChange> showPackets;
	private final Collection<PacketPlayOutWorldEvent> destroyPackets;
	private final Collection<PacketPlayOutBlockChange> hidePackets;
	private final List<Location> allocatedBlocks;
	private final String clientData;

	public static Allocation allocate(SubjectInfo info, SubjectPrototype prototype, Location origin) {
		if (origin == null) return null;

		Box box = prototype.getBox();
		List<PacketPlayOutBlockChange> showPackets = new ArrayList<>();
		List<PacketPlayOutWorldEvent> destroyPackets = new ArrayList<>();
		List<PacketPlayOutBlockChange> hidePackets = new ArrayList<>();
		List<Location> allocated = new ArrayList<>();

		V3 absoluteOrigin = UtilV3.fromVector(origin.toVector());
		V3 relativeOrigin = box.getDimensions().clone().mult(0.5);
		relativeOrigin.setY(0);

		int minX = Integer.MAX_VALUE;
		int minY = Integer.MAX_VALUE;
		int minZ = Integer.MAX_VALUE;
		int maxX = Integer.MIN_VALUE;
		int maxY = Integer.MIN_VALUE;
		int maxZ = Integer.MIN_VALUE;

		for (int x = (int) box.getMin().getX(); x <= box.getMax().getX(); x++) {
			for (int y = (int) box.getMin().getY(); y <= box.getMax().getY(); y++) {
				for (int z = (int) box.getMin().getZ(); z <= box.getMax().getZ(); z++) {
					Location dst = box.transpose(absoluteOrigin, info.getRotation(), relativeOrigin, x, y, z);

					if (minX > dst.getBlockX()) minX = dst.getBlockX();
					if (minY > dst.getBlockY()) minY = dst.getBlockY();
					if (minZ > dst.getBlockZ()) minZ = dst.getBlockZ();
					if (maxX < dst.getBlockX()) maxX = dst.getBlockX();
					if (maxY < dst.getBlockY()) maxY = dst.getBlockY();
					if (maxZ < dst.getBlockZ()) maxZ = dst.getBlockZ();

					// dst.clone().subtract(0, 1, 0).getBlock().getType() != Material.MELON_BLOCK
					if (dst.getBlock().getType() != Material.AIR)
						return null; // Невозможно разместить субъект - место занято или не доступно.
					Location src = new Location(App.getApp().getWorld(), x, y, z);
					if (src.getBlock().getType() == Material.AIR) continue;


					World world = App.getApp().getNMSWorld();
					BlockPosition pos = nms(dst);
					PacketPlayOutBlockChange showPacket = new PacketPlayOutBlockChange(world, pos);
					IBlockData data = world.getType(nms(src));
					if (data.getBlock() == Blocks.dR) {
						data = Blocks.dR.getBlockData().set(BlockCloth.COLOR, EnumColor.fromColorIndex(info.getColor().getWoolData()));
					}
					showPacket.block = data;
					showPackets.add(showPacket);
					int tileId = Block.getCombinedId(data);
					destroyPackets.add(new PacketPlayOutWorldEvent(2001, pos, tileId, false));
					hidePackets.add(new PacketPlayOutBlockChange(world, pos));
					allocated.add(dst);
				}
			}
		}

		String clientData = minX + "_" + minY + "_" + minZ + "_" + maxX + "_" + maxY + "_" + maxZ;

		return new Allocation(origin, showPackets, destroyPackets, hidePackets, allocated, clientData);
	}

}
