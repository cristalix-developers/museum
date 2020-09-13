package museum.util;

import lombok.Data;
import net.minecraft.server.v1_12_R1.*;

import java.util.ArrayList;
import java.util.List;

@Data
public class ChunkWriter {

	public static final IBlockData AIR_DATA = Block.getById(0).getBlockData();
	private final List<BlockPosition> modified = new ArrayList<>();
	private final Chunk chunk;
	private PacketPlayOutMapChunk readyPacket = null;

	public void write(BlockPosition position, IBlockData blockData) {
		chunk.a(position, blockData);
		modified.add(position);
	}

	public PacketPlayOutMapChunk build(int flags) {
		PacketPlayOutMapChunk packet = readyPacket != null ? readyPacket : new PacketPlayOutMapChunk(chunk, flags);
		for (BlockPosition position : modified) chunk.a(position, AIR_DATA);
		return packet;
	}

}
