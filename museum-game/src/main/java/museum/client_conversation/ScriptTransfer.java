package museum.client_conversation;

import io.netty.buffer.Unpooled;
import museum.player.User;
import net.minecraft.server.v1_12_R1.ItemStack;
import net.minecraft.server.v1_12_R1.PacketDataSerializer;
import net.minecraft.server.v1_12_R1.PacketPlayOutCustomPayload;
import ru.cristalix.core.GlobalSerializers;

/**
 * @author func 02.01.2021
 * @project museum
 */
public class ScriptTransfer {
	private final PacketDataSerializer serializer = new PacketDataSerializer(Unpooled.buffer());

	public ScriptTransfer json(Object object) {
		return string(GlobalSerializers.toJson(object));
	}

	public ScriptTransfer string(String string) {
		serializer.writeString(string);
		return this;
	}

	public ScriptTransfer item(ItemStack item) {
		serializer.writeItem(item);
		return this;
	}

	public ScriptTransfer integer(int integer) {
		serializer.writeInt(integer);
		return this;
	}

	public void send(String channel, User user) {
		user.sendPacket(new PacketPlayOutCustomPayload(channel, serializer));
	}
}