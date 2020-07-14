package ru.cristalix.museum.museum.subject;

import clepto.cristalix.Box;
import lombok.Data;
import net.minecraft.server.v1_12_R1.*;
import org.bukkit.Location;
import org.bukkit.Material;
import ru.cristalix.core.math.V3;
import ru.cristalix.core.util.UtilV3;
import ru.cristalix.museum.App;
import ru.cristalix.museum.data.subject.SubjectInfo;
import ru.cristalix.museum.museum.map.SubjectPrototype;

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

		for (int x = (int) box.getMin().getX(); x <= box.getMax().getX(); x++) {
			for (int y = (int) box.getMin().getY(); y <= box.getMax().getY(); y++) {
				for (int z = (int) box.getMin().getZ(); z <= box.getMax().getZ(); z++) {
					Location dst = box.transpose(absoluteOrigin, info.getRotation(), relativeOrigin, x, y, z);
					if (dst.getBlock().getType() != Material.AIR || dst.clone().subtract(0, 1, 0).getBlock().getType() != Material.MELON_BLOCK)
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

		return new Allocation(origin, showPackets, destroyPackets, hidePackets, allocated);
	}

}
