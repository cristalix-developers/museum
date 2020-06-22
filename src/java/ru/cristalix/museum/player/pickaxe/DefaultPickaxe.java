package ru.cristalix.museum.player.pickaxe;

import net.minecraft.server.v1_12_R1.BlockPosition;
import net.minecraft.server.v1_12_R1.EnumDirection;
import ru.cristalix.museum.excavation.Excavation;
import ru.cristalix.museum.player.User;

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
				.filter(p -> Excavation.isAir(user, p))
				.collect(Collectors.toList());
	}

}
