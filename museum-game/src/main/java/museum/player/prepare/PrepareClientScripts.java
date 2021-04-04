package museum.player.prepare;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import lombok.val;
import museum.App;
import museum.player.User;
import net.minecraft.server.v1_12_R1.PacketDataSerializer;
import net.minecraft.server.v1_12_R1.PacketPlayOutCustomPayload;
import ru.cristalix.core.display.DisplayChannels;
import ru.cristalix.core.display.messages.Mod;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

/**
 * @author func 13.06.2020
 * @project Museum
 */
public class PrepareClientScripts implements Prepare {

	public static final Prepare INSTANCE = new PrepareClientScripts();

	private final List<ByteBuf> packets = new ArrayList<>();

	public PrepareClientScripts() {
		try {
			val dir = new File("./mods/");
			for (val file : dir.listFiles()) {
				byte[] serialize = Mod.serialize(new Mod(Files.readAllBytes(file.toPath())));
				ByteBuf buffer = Unpooled.buffer();
				buffer.writeBytes(serialize);
				packets.add(buffer);
			}
		} catch (Exception exception) {
			throw new RuntimeException(exception);
		}
	}

	@Override
	public void execute(User user, App app) {
		for (val byteBuf : packets) {
			user.sendPacket(new PacketPlayOutCustomPayload(
					DisplayChannels.MOD_CHANNEL,
					new PacketDataSerializer(byteBuf.retainedSlice())
			));
		}
	}
}