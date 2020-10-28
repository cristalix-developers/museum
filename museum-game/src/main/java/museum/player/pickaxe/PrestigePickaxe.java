package museum.player.pickaxe;

import museum.player.User;
import net.minecraft.server.v1_12_R1.BlockPosition;

import java.util.ArrayList;
import java.util.List;

/**
 * @author func 24.05.2020
 * @project Museum
 */
public class PrestigePickaxe implements Pickaxe {

	@Override
	public List<BlockPosition> dig(User user, BlockPosition pos) {
		List<BlockPosition> list = new ArrayList<>();
		BlockPosition[] positions = {
				pos.east().east(),
				pos.west().west(),
				pos.south().south(),
				pos.north().north()
		};
		for (BlockPosition blockPosition : positions) {
			if (breakBlock(user, blockPosition)) {
				list.add(blockPosition);
			}
		}
		return list;
	}

}
