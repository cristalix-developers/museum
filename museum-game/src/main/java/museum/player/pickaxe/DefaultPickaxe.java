package museum.player.pickaxe;

import museum.player.User;
import net.minecraft.server.v1_12_R1.BlockPosition;
import net.minecraft.server.v1_12_R1.EnumDirection;

import java.util.ArrayList;
import java.util.List;

/**
 * @author func 24.05.2020
 * @project Museum
 */
public class DefaultPickaxe implements Pickaxe {

	@Override
	public List<BlockPosition> dig(User user, BlockPosition pos) {
		List<BlockPosition> list = new ArrayList<>();
		for (EnumDirection enumDirection : EnumDirection.values()) {
			BlockPosition blockPosition = pos.shift(enumDirection);
			if (breakBlock(user, blockPosition)) {
				list.add(blockPosition);
			}
		}
		return list;
	}

}
