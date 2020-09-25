package museum.player.pickaxe;

import lombok.val;
import museum.App;
import museum.excavation.Excavation;
import museum.player.User;
import museum.util.ChunkWriter;
import net.minecraft.server.v1_12_R1.BlockPosition;
import net.minecraft.server.v1_12_R1.PacketPlayOutBlockBreakAnimation;
import net.minecraft.server.v1_12_R1.PacketPlayOutBlockChange;
import net.minecraft.server.v1_12_R1.PlayerConnection;
import org.bukkit.util.Vector;

import java.util.List;

public interface Pickaxe {

	List<BlockPosition> dig(User user, BlockPosition position);

	default boolean breakBlock(User user, BlockPosition position) {
		if (Excavation.isAir(user, position)) {
			val blockChange = new PacketPlayOutBlockChange(App.getApp().getNMSWorld(), position);
			blockChange.block = ChunkWriter.AIR_DATA;
			user.sendPacket(blockChange);
			return true;
		}
		return false;
	}

	default void animate(PlayerConnection connection, BlockPosition position) {
		connection.sendPacket(new PacketPlayOutBlockBreakAnimation(
				Vector.random.nextInt(1000),
				position,
				6 + Vector.random.nextInt(3)
		));
	}

}
