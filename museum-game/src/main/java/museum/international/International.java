package museum.international;

import museum.player.State;
import museum.player.User;
import net.minecraft.server.v1_12_R1.BlockPosition;
import net.minecraft.server.v1_12_R1.PacketPlayInBlockDig;

public interface International extends State {

	void acceptBlockBreak(User user, PacketPlayInBlockDig packet);

	boolean canBeBroken(BlockPosition pos);

}
