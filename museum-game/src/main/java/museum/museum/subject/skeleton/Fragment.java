package museum.museum.subject.skeleton;

import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap;
import lombok.Data;
import net.minecraft.server.v1_12_R1.Packet;
import net.minecraft.server.v1_12_R1.PacketListenerPlayOut;

import java.util.Collection;
import java.util.Map;

import static museum.museum.subject.skeleton.Displayable.orientedOffset;

@Data
public class Fragment implements Displayable {

	private final String address;
	private final Map<Piece, V4> pieceOffsetMap = new Reference2ObjectArrayMap<>();

	@Override
	public void getShowPackets(Collection<Packet<PacketListenerPlayOut>> buffer, V4 position) {
		pieceOffsetMap.forEach((piece, offset) -> piece.getShowPackets(buffer, orientedOffset(position, offset)));
	}

	@Override
	public void getUpdatePackets(Collection<Packet<PacketListenerPlayOut>> buffer, V4 position) {
		pieceOffsetMap.forEach((piece, offset) -> piece.getUpdatePackets(buffer, orientedOffset(position, offset)));
	}

	@Override
	public void getHidePackets(Collection<Packet<PacketListenerPlayOut>> buffer) {
		pieceOffsetMap.keySet().forEach(piece -> piece.getHidePackets(buffer));
	}


}
