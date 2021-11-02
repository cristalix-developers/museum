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
public class ModTransfer {
	private final PacketDataSerializer serializer = new PacketDataSerializer(Unpooled.buffer());

	public ModTransfer json(Object object) {
		return string(GlobalSerializers.toJson(object));
	}

	public ModTransfer string(String string) {
		serializer.writeString(string);
		return this;
	}

	public ModTransfer item(ItemStack item) {
		serializer.writeItem(item);
		return this;
	}

	public ModTransfer integer(int integer) {
		serializer.writeInt(integer);
		return this;
	}

	public ModTransfer integers(int... integers) {
		serializer.writeIntArray(integers);
		return this;
	}

	public ModTransfer pointed(double pointed) {
		serializer.writeDouble(pointed);
		return this;
	}

	public void send(String channel, User user) {
		user.sendPacket(new PacketPlayOutCustomPayload(channel, serializer));
	}
}