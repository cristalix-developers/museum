package museum.display;

import museum.player.User;
import net.minecraft.server.v1_12_R1.Packet;
import net.minecraft.server.v1_12_R1.PacketListenerPlayOut;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.Collection;

public interface Displayable {

	void getShowPackets(Collection<Packet<PacketListenerPlayOut>> buffer, V5 position);

	default void show(User user, V5 position) {
		Collection<Packet<PacketListenerPlayOut>> packets = new ArrayList<>();
		this.getShowPackets(packets, position);
		packets.forEach(user::sendPacket);
	}

	default void show(User user) {
		show(user, null);
	}

	void getUpdatePackets(Collection<Packet<PacketListenerPlayOut>> buffer, V5 position);

	default void update(User user, V5 position) {
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

}
