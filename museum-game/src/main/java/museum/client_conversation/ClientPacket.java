package museum.client_conversation;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import lombok.val;
import museum.player.User;
import net.minecraft.server.v1_12_R1.PacketDataSerializer;
import net.minecraft.server.v1_12_R1.PacketPlayOutCustomPayload;
import ru.cristalix.core.util.UtilNetty;

public class ClientPacket<D> {

	private final String channel;

	public ClientPacket(String channel) {
		this.channel = channel;
	}

	public void send(User user, D data) {
		val byteBuf = Unpooled.buffer();
		UtilNetty.writeString(byteBuf, data.toString());
		user.sendPacket(new PacketPlayOutCustomPayload("museum:" + channel, new PacketDataSerializer(byteBuf)));
	}
}