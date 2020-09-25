package museum.player.pickaxe;

import museum.player.User;
import net.minecraft.server.v1_12_R1.BlockPosition;
import net.minecraft.server.v1_12_R1.EnumDirection;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author func 24.05.2020
 * @project Museum
 */
public class DefaultPickaxe implements Pickaxe {

	@Override
	public List<BlockPosition> dig(User user, BlockPosition pos) {
		return Arrays.stream(EnumDirection.values())
				.map(pos::shift)
				.filter(p -> breakBlock(user, p))
				.collect(Collectors.toList());
	}

}
