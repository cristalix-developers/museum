package museum.museum.subject;

import clepto.bukkit.world.Box;
import com.google.common.collect.Maps;
import implario.math.V3;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.val;
import museum.App;
import museum.museum.map.SubjectPrototype;
import museum.museum.subject.skeleton.AtomPiece;
import museum.museum.subject.skeleton.Displayable;
import museum.museum.subject.skeleton.Piece;
import museum.museum.subject.skeleton.V4;
import museum.player.State;
import museum.player.User;
import museum.util.ChunkWriter;
import museum.util.LocationUtil;
import net.minecraft.server.v1_12_R1.*;
import org.bukkit.Location;
import org.bukkit.Material;
import ru.cristalix.core.formatting.Color;

import java.util.*;
import java.util.function.Function;

import static clepto.bukkit.B.nms;
import static museum.util.Colorizer.applyColor;

@Data
public class Allocation {

	private final Location origin;
	private final Map<BlockPosition, IBlockData> blocks;
	private final Collection<Packet<PacketListenerPlayOut>> updatePackets;
	private final Collection<Packet<PacketListenerPlayOut>> removePackets;
	private final Map<AtomPiece, V4> pieces = new HashMap<>();
	private final List<Displayable> displayables = new ArrayList<>();
	private final State state;
	private final List<Location> allocatedBlocks;
	private final ru.cristalix.core.math.V3 min;
	private final ru.cristalix.core.math.V3 max;

	public static Allocation allocate(State owner, Color color, SubjectPrototype prototype, Location origin) {
		if (origin == null) return null;

		Box box = prototype.getBox();
		Map<BlockPosition, IBlockData> blocks = Maps.newHashMap();
		List<Location> allocated = new ArrayList<>();

		V3 absoluteOrigin = V3.of(origin.getX(), origin.getY(), origin.getZ());
		V3 relativeOrigin = box.getDimensions().multiply(0.5);
		relativeOrigin = relativeOrigin.withY(0);

		int minX = Integer.MAX_VALUE;
		int minY = Integer.MAX_VALUE;
		int minZ = Integer.MAX_VALUE;
		int maxX = Integer.MIN_VALUE;
		int maxY = Integer.MIN_VALUE;
		int maxZ = Integer.MIN_VALUE;
		World world = App.getApp().getNMSWorld();

		@AllArgsConstructor
		class BlockDataUnit {

			final short offset;
			final IBlockData blockData;

		}

		Map<ChunkCoordIntPair, List<BlockDataUnit>> chunkMap = new HashMap<>();

		for (int x = (int) box.getMin().getX(); x <= box.getMax().getX(); x++) {
			for (int y = (int) box.getMin().getY(); y <= box.getMax().getY(); y++) {
				for (int z = (int) box.getMin().getZ(); z <= box.getMax().getZ(); z++) {
					val dst = box.transpose(absoluteOrigin, LocationUtil.getOrientation(origin), relativeOrigin, x, y, z);
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

					IBlockData data = applyColor(world.getType(nms(src)), color);

					int xOffset = blockPos.getX() - (blockPos.getX() >> 4) * 16;
					int yOffset = blockPos.getY();
					int zOffset = blockPos.getZ() - (blockPos.getZ() >> 4) * 16;
					short offset = (short) ((short) (((short) xOffset & 0xF) << 12) | (yOffset & 0xFF) % 256 | (zOffset & 0xFF) << 8);

					BlockDataUnit blockData = new BlockDataUnit(offset, data);
					chunkMap.computeIfAbsent(chunkPos, c -> new ArrayList<>()).add(blockData);

					allocated.add(dst);

					blocks.put(blockPos, blockData.blockData);
				}
			}
		}

		Collection<Packet<PacketListenerPlayOut>> updatePackets = new ArrayList<>();
		Collection<Packet<PacketListenerPlayOut>> removePackets = new ArrayList<>();

		for (val entry : chunkMap.entrySet()) {
			PacketPlayOutMultiBlockChange updatePacket = new PacketPlayOutMultiBlockChange();
			PacketPlayOutMultiBlockChange removePacket = new PacketPlayOutMultiBlockChange();
			updatePacket.a = removePacket.a = entry.getKey();
			List<BlockDataUnit> list = entry.getValue();
			updatePacket.b = new PacketPlayOutMultiBlockChange.MultiBlockChangeInfo[list.size()];
			removePacket.b = new PacketPlayOutMultiBlockChange.MultiBlockChangeInfo[list.size()];
			for (int i = 0; i < list.size(); i++) {
				BlockDataUnit blockData = list.get(i);
				updatePacket.b[i] = updatePacket.new MultiBlockChangeInfo(blockData.offset, blockData.blockData);
				removePacket.b[i] = removePacket.new MultiBlockChangeInfo(blockData.offset, ChunkWriter.AIR_DATA);
			}
			updatePackets.add(updatePacket);
			removePackets.add(removePacket);
		}
		return new Allocation(origin, blocks, updatePackets, removePackets, owner, allocated,
				new ru.cristalix.core.math.V3(minX, minY, minZ), new ru.cristalix.core.math.V3(maxX, maxY, maxZ)
		);
	}

	public void prepareUpdate(Function<IBlockData, IBlockData> converter) {
		for (val rawPacket : updatePackets) {
			if (rawPacket.getClass() != PacketPlayOutMultiBlockChange.class)
				continue;
			PacketPlayOutMultiBlockChange packet = (PacketPlayOutMultiBlockChange) rawPacket;
			for (int i = 0; i < packet.b.length; i++) {
				IBlockData newData = converter.apply(packet.b[i].c);
				blocks.put(packet.b[i].a(), newData);
				packet.b[i] = packet.new MultiBlockChangeInfo(packet.b[i].b, newData);
			}
		}
	}

	public void allocateDisplayable(Displayable displayable) {
		displayables.add(displayable);
	}

	public void removeDisplayable(Displayable displayable) {
		displayables.remove(displayable);
	}

	public void allocatePiece(Piece piece, V4 origin, boolean update) {
		Map<AtomPiece, V4> freshPieces = new HashMap<>();
		piece.recursiveTraverse(freshPieces, origin);
		if (update) {
			List<Packet<PacketListenerPlayOut>> packets = new ArrayList<>();
			freshPieces.forEach((atom, position) -> {
				V4 existing = this.pieces.get(atom);
				if (existing != null) atom.getUpdatePackets(packets, position);
				else atom.getShowPackets(packets, position);
			});
			for (User user : state.getUsers()) {
				// Если элемент слишком далеко не отправлять
				if (user.getPlayer() == null || user.getLocation().distanceSquared(origin.toLocation(user.getWorld())) > 2000)
					continue;
				packets.forEach(user::sendPacket);
			}
		}
		this.pieces.putAll(freshPieces);
	}

	public void removePiece(Piece piece) {
		Map<AtomPiece, V4> piecesToRemove = new HashMap<>();
		piece.recursiveTraverse(piecesToRemove, new V4(0, 0, 0, 0));
		int[] ids = new int[piecesToRemove.size()];
		int i = 0;
		for (AtomPiece atomPiece : piecesToRemove.keySet()) {
			this.pieces.remove(atomPiece);
			ids[i++] = atomPiece.getStand().id;
		}
		PacketPlayOutEntityDestroy packet = new PacketPlayOutEntityDestroy(ids);
		for (User user : state.getUsers())
			if (user.getPlayer() != null)
				user.sendPacket(packet);
	}

	public void perform(User user, Action... actions) {
		this.perform(Collections.singleton(user), null, actions);
	}

	public void perform(User users, Chunk chunk, Action... actions) {
		this.perform(Collections.singleton(users), chunk, actions);
	}

	public void perform(Action... actions) {
		this.perform(state.getUsers(), null, actions);
	}

	public void perform(Collection<User> users, Action... actions) {
		this.perform(users, null, actions);
	}

	public void perform(Collection<User> users, Chunk chunk, Action... actions) {
		List<Packet<PacketListenerPlayOut>> packets = new ArrayList<>();
		for (Action action : actions)
			action.perform(this, packets, chunk);
		for (User user : users)
			for (val packet : packets)
				user.sendPacket(packet);
	}

	@Override
	public String toString() {
		return origin + " ";
	}

	@RequiredArgsConstructor
	public enum Action {
		SPAWN_PIECES((allocation, buffer, chunk) -> {
			allocation.pieces.forEach((piece, position) -> {
				if (chunk == null || chunk.locX == (int) position.x >> 4 && chunk.locZ == (int) position.z >> 4)
					piece.getShowPackets(buffer, position);
			});
		}), SPAWN_DISPLAYABLE((allocation, buffer, chunk) -> {
			allocation.displayables.forEach(displayable -> displayable.getShowPackets(buffer, null));
		}), DESTROY_DISPLAYABLE((allocation, buffer, chunk) -> {
			allocation.displayables.forEach(displayable -> displayable.getHidePackets(buffer));
		}), HIDE_BLOCKS((allocation, buffer, chunk) -> buffer.addAll(allocation.removePackets)),
		HIDE_PIECES((allocation, buffer, chunk) -> {
			int[] ids = new int[allocation.pieces.size()];
			int i = 0;
			for (AtomPiece piece : allocation.pieces.keySet()) ids[i++] = piece.getStand().id;
			buffer.add(new PacketPlayOutEntityDestroy(ids));
		}),
		UPDATE_BLOCKS((allocation, buffer, chunk) -> buffer.addAll(allocation.updatePackets)),
		UPDATE_PIECES((allocation, buffer, chunk) -> {
			allocation.pieces.forEach((piece, position) -> piece.getUpdatePackets(buffer, position));
		}),
		PLAY_EFFECTS((allocation, buffer, chunk) -> {
			// ToDo: партиклов слишком много, лагает
			for (val rawPacket : allocation.updatePackets) {
				if (rawPacket.getClass() != PacketPlayOutMultiBlockChange.class)
					continue;
				PacketPlayOutMultiBlockChange packet = (PacketPlayOutMultiBlockChange) rawPacket;
				for (PacketPlayOutMultiBlockChange.MultiBlockChangeInfo info : packet.b) {
					if (info == null) continue;
					// 2001 - id события разрушения блока
					buffer.add(new PacketPlayOutWorldEvent(2001, info.a(), Block.getCombinedId(info.c), false));
				}
			}
		});

		private final Executor executor;

		public void perform(Allocation allocation, List<Packet<PacketListenerPlayOut>> resultBuffer, Chunk chunk) {
			this.executor.execute(allocation, resultBuffer, chunk);
		}

		@FunctionalInterface
		public interface Executor {

			void execute(Allocation allocation, List<Packet<PacketListenerPlayOut>> resultBuffer, Chunk chunk);

		}
	}
}
