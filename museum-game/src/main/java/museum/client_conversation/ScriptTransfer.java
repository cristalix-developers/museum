package museum.client_conversation;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import museum.player.User;
import net.minecraft.server.v1_12_R1.ItemStack;
import net.minecraft.server.v1_12_R1.PacketDataSerializer;
import net.minecraft.server.v1_12_R1.PacketPlayOutCustomPayload;
import ru.cristalix.core.GlobalSerializers;
import ru.cristalix.core.util.UtilNetty;

/**
 * @author func 02.01.2021
 * @project museum
 */
public class ScriptTransfer {
	private final ByteBuf buffer = Unpooled.buffer();

	public ScriptTransfer json(Object object) {
		return string(GlobalSerializers.toJson(object));
	}

	public ScriptTransfer string(String string) {
		UtilNetty.writeString(buffer, string);
		return this;
	}

	public ScriptTransfer item(ItemStack item) {
		new PacketDataSerializer(buffer).writeItem(item);
		return this;
	}

	public ScriptTransfer integer(int integer) {
		buffer.writeInt(integer);
		return this;
	}

	public void send(String channel, User user) {
		user.sendPacket(new PacketPlayOutCustomPayload(channel, new PacketDataSerializer(buffer)));
	}
}