package museum.museum.subject;

import clepto.cristalix.mapservice.Box;
import com.google.common.collect.Maps;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.val;
import museum.App;
import museum.data.SubjectInfo;
import museum.museum.map.SubjectPrototype;
import museum.player.pickaxe.Pickaxe;
import net.minecraft.server.v1_12_R1.*;
import org.bukkit.Location;
import org.bukkit.Material;
import ru.cristalix.core.math.V3;
import ru.cristalix.core.util.UtilV3;

import java.util.*;

import static clepto.bukkit.B.nms;

@Data
public class Allocation {

	private final Location origin;
	private final Map<BlockPosition, IBlockData> blocks;
	private final Collection<PacketPlayOutWorldEvent> destroyPackets;
	private final List<Location> allocatedBlocks;
	private final String clientData;

	public static Allocation allocate(SubjectInfo info, SubjectPrototype prototype, Location origin) {
		if (origin == null) return null;

		Box box = prototype.getBox();
		Map<BlockPosition, IBlockData> blocks = Maps.newHashMap();
		List<PacketPlayOutWorldEvent> destroyPackets = new ArrayList<>();
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
		World world = App.getApp().getNMSWorld();

		@AllArgsConstructor
		class BlockData {

			final short offset;
			final IBlockData blockData;

		}

		Map<ChunkCoordIntPair, List<BlockData>> chunkMap = new HashMap<>();

		for (int x = (int) box.getMin().getX(); x <= box.getMax().getX(); x++) {
			for (int y = (int) box.getMin().getY(); y <= box.getMax().getY(); y++) {
				for (int z = (int) box.getMin().getZ(); z <= box.getMax().getZ(); z++) {
					val dst = box.transpose(absoluteOrigin, info.getRotation(), relativeOrigin, x, y, z);
					val src = new Location(App.getApp().getWorld(), x, y, z);

					if (src.getBlock().getType() == Material.AIR) continue;

					if (minX > dst.getBlockX()) minX = dst.getBlockX();
					if (minY > dst.getBlockY()) minY = dst.getBlockY();
					if (minZ > dst.getBlockZ()) minZ = dst.getBlockZ();
					if (maxX < dst.getBlockX()) maxX = dst.getBlockX();
					if (maxY < dst.getBlockY()) maxY = dst.getBlockY();
					if (maxZ < dst.getBlockZ()) maxZ = dst.getBlockZ();

					BlockPosition blockPos = nms(dst);
					ChunkCoordIntPair chunkPos = new ChunkCoordIntPair(blockPos);

					IBlockData data = world.getType(nms(src));
					if (data.getBlock() == Blocks.dR) {
						data = Blocks.dR.getBlockData().set(BlockCloth.COLOR, EnumColor.fromColorIndex(info.getColor().getWoolData()));
					}
					int xOffset = blockPos.getX() - (blockPos.getX() >> 4) * 16;
					int yOffset = blockPos.getY();
					int zOffset = blockPos.getZ() - (blockPos.getZ() >> 4) * 16;
					short offset = (short) ((short) (((short) xOffset & 0xF) << 12) | (yOffset & 0xFF) % 256 | (zOffset & 0xFF) << 8);

					BlockData blockData = new BlockData(offset, data);
					chunkMap.computeIfAbsent(chunkPos, c -> new ArrayList<>()).add(blockData);

					int tileId = Block.getCombinedId(data);
					destroyPackets.add(new PacketPlayOutWorldEvent(2001, blockPos, tileId, false));
					allocated.add(dst);

					blocks.put(blockPos, blockData.blockData);
				}
			}
		}

		String clientData = minX + "_" + minY + "_" + minZ + "_" + maxX + "_" + maxY + "_" + maxZ;

		return new Allocation(origin, blocks, destroyPackets, allocated, clientData);
	}

	@Override
	public String toString() {
		return origin + " " + clientData;
	}

}
