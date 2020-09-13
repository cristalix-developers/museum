package museum.museum.subject.skeleton;

import museum.player.User;
import net.minecraft.server.v1_12_R1.Packet;
import net.minecraft.server.v1_12_R1.PacketListenerPlayOut;

import java.util.ArrayList;
import java.util.Collection;

public interface Displayable {

	void getShowPackets(Collection<Packet<PacketListenerPlayOut>> buffer, V4 position);

	default void show(User user, V4 position) {
		Collection<Packet<PacketListenerPlayOut>> packets = new ArrayList<>();
		this.getShowPackets(packets, position);
		packets.forEach(user::sendPacket);
	}

	void getUpdatePackets(Collection<Packet<PacketListenerPlayOut>> buffer, V4 position);

	default void update(User user, V4 position) {
		Collection<Packet<PacketListenerPlayOut>> packets = new ArrayList<>();
		this.getUpdatePackets(packets, position);
		packets.forEach(user::sendPacket);
	}

	void getHidePackets(Collection<Packet<PacketListenerPlayOut>> buffer);

	default void hide(User user) {
		Collection<Packet<PacketListenerPlayOut>> packets = new ArrayList<>();
		this.getHidePackets(packets);
		packets.forEach(user::sendPacket);
	}

	static V4 orientedOffset(V4 positionRotation, V4 offset) {
		V4 orientedOffset = offset.clone().rotate(V4.Y, positionRotation.rot);
		return V4.sum(positionRotation, orientedOffset).setRot(orientedOffset.rot);
	}

}
