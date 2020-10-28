package museum.player.pickaxe;

import museum.player.User;
import net.minecraft.server.v1_12_R1.BlockPosition;
import net.minecraft.server.v1_12_R1.PlayerConnection;

import java.util.Collections;
import java.util.List;

/**
 * @author func 24.05.2020
 * @project Museum
 */
public class ProfessionalPickaxe implements Pickaxe {

	@Override
	public List<BlockPosition> dig(User user, BlockPosition pos) {
		PlayerConnection connection = user.getConnection();
		animate(connection, pos.west().west());
		animate(connection, pos.south().south());
		animate(connection, pos.east().east());
		animate(connection, pos.north().north());
		animate(connection, pos.west().south());
		animate(connection, pos.south().west());
		animate(connection, pos.east().north());
		animate(connection, pos.north().east());
		return Collections.emptyList();
	}

}
