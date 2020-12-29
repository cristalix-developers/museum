package museum.client_conversation;

import io.netty.buffer.Unpooled;
import lombok.val;
import museum.player.User;
import net.minecraft.server.v1_12_R1.PacketDataSerializer;
import net.minecraft.server.v1_12_R1.PacketPlayOutCustomPayload;
import ru.cristalix.core.util.UtilNetty;

public class ClientPacket {

	private final String channel;
	private static final ClientPacket TITLE_CAST = new ClientPacket("museumcast");

	public ClientPacket(String channel) {
		this.channel = channel;
	}

	public void send(User user, String data) {
		val byteBuf = Unpooled.buffer();
		UtilNetty.writeString(byteBuf, data);
		user.sendPacket(new PacketPlayOutCustomPayload(channel, new PacketDataSerializer(byteBuf)));
	}

	public static void sendTopTitle(User user, String text) {
		TITLE_CAST.send(user, text);
	}
}