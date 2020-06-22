package ru.cristalix.museum.player.pickaxe;

import lombok.val;
import net.minecraft.server.v1_12_R1.*;
import ru.cristalix.museum.App;
import ru.cristalix.museum.excavation.Excavation;
import ru.cristalix.museum.player.User;

import java.util.List;
import java.util.Random;

public interface Pickaxe {

	Random RANDOM = new Random();

	IBlockData AIR_DATA = Block.getById(0).getBlockData();

	List<BlockPosition> dig(User user, BlockPosition position);

	default boolean breakBlock(User user, BlockPosition position) {
		if (Excavation.isAir(user, position)) {
			val blockChange = new PacketPlayOutBlockChange(App.getApp().getNMSWorld(), position);
			blockChange.block = AIR_DATA;
			user.sendPacket(blockChange);
			return true;
		}
		return false;
	}

	default void animate(PlayerConnection connection, BlockPosition position) {
		connection.sendPacket(new PacketPlayOutBlockBreakAnimation(
				RANDOM.nextInt(1000),
				position,
				6 + RANDOM.nextInt(3)
		));
	}

}
