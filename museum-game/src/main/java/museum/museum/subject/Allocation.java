package museum.museum.subject;

import clepto.cristalix.mapservice.Box;
import com.google.common.collect.Maps;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.val;
import museum.App;
import museum.data.SubjectInfo;
import museum.museum.map.SubjectPrototype;
import museum.player.User;
import net.minecraft.server.v1_12_R1.*;
import org.bukkit.Location;
import org.bukkit.Material;
import ru.cristalix.core.math.V3;
import ru.cristalix.core.util.UtilV3;

import java.util.*;
import java.util.function.Function;

import static clepto.bukkit.B.nms;
import static museum.util.Colorizer.applyColor;

@Data
public class Allocation {

	private final Location origin;
	private final Map<BlockPosition, IBlockData> blocks;
	private final Collection<PacketPlayOutMultiBlockChange> updatePackets;
	private final List<Location> allocatedBlocks;
	private final String clientData;

	public static Allocation allocate(SubjectInfo info, SubjectPrototype prototype, Location origin) {
		if (origin == null) return null;

		Box box = prototype.getBox();
		Map<BlockPosition, IBlockData> blocks = Maps.newHashMap();
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

					IBlockData data = applyColor(world.getType(nms(src)), info.getColor());

					int xOffset = blockPos.getX() - (blockPos.getX() >> 4) * 16;
					int yOffset = blockPos.getY();
					int zOffset = blockPos.getZ() - (blockPos.getZ() >> 4) * 16;
					short offset = (short) ((short) (((short) xOffset & 0xF) << 12) | (yOffset & 0xFF) % 256 | (zOffset & 0xFF) << 8);

					BlockData blockData = new BlockData(offset, data);
					chunkMap.computeIfAbsent(chunkPos, c -> new ArrayList<>()).add(blockData);

					int tileId = Block.getCombinedId(data);
					allocated.add(dst);

					blocks.put(blockPos, blockData.blockData);
				}
			}
		}

		Collection<PacketPlayOutMultiBlockChange> updatePackets = new ArrayList<>();

		for (Map.Entry<ChunkCoordIntPair, List<BlockData>> entry : chunkMap.entrySet()) {
			PacketPlayOutMultiBlockChange updatePacket = new PacketPlayOutMultiBlockChange();
			updatePacket.a = entry.getKey();
			List<BlockData> list = entry.getValue();
			updatePacket.b = new PacketPlayOutMultiBlockChange.MultiBlockChangeInfo[list.size()];
			for (int i = 0; i < list.size(); i++) {
				BlockData blockData = list.get(i);
				updatePacket.b[i] = updatePacket.new MultiBlockChangeInfo(blockData.offset, blockData.blockData);
			}
			updatePackets.add(updatePacket);
		}

		String clientData = minX + "_" + minY + "_" + minZ + "_" + maxX + "_" + maxY + "_" + maxZ;

		return new Allocation(origin, blocks, updatePackets, allocated, clientData);
	}

	public void prepareUpdate(Function<IBlockData, IBlockData> converter) {
		for (PacketPlayOutMultiBlockChange packet : updatePackets) {
			for (int i = 0; i < packet.b.length; i++) {
				packet.b[i] = packet.new MultiBlockChangeInfo(packet.b[i].b, converter.apply(packet.b[i].c));
			}
		}
	}

	/**
	 * Частицы и звуки ломания блоков в этой аллокации
	 */
	public void sendDestroyEffects(Collection<User> users) {
		// ToDo: партиклов слишком много, лагает!!!
		List<PacketPlayOutWorldEvent> packets = new ArrayList<>();
		for (PacketPlayOutMultiBlockChange packet : this.updatePackets) {
			for (PacketPlayOutMultiBlockChange.MultiBlockChangeInfo info : packet.b) {
				if (info == null) continue;
				// 2001 - id события разрушения блока
				packets.add(new PacketPlayOutWorldEvent(2001, info.a(), Block.getCombinedId(info.c), false));
			}
		}
		sendPackets(packets, users);
	}

	public void sendUpdate(Collection<User> users) {
		sendPackets(this.updatePackets, users);
	}

	public static void sendPackets(Collection<? extends Packet<?>> packets, User... users) {
		sendPackets(packets, Arrays.asList(users));
	}
	public static void sendPackets(Collection<? extends Packet<?>> packets, Collection<User> users) {
		for (User user : users) {
			for (Packet<?> packet : packets)
				user.sendPacket(packet);
		}
	}

	@Override
	public String toString() {
		return origin + " " + clientData;
	}

}
