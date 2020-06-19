package ru.cristalix.museum.player.pickaxe;

import net.minecraft.server.v1_12_R1.BlockPosition;
import net.minecraft.server.v1_12_R1.PlayerConnection;
import ru.cristalix.museum.player.User;

import java.util.List;

/**
 * @author func 24.05.2020
 * @project Museum
 */
public class ProfessionalPickaxe implements Pickaxe {

    @Override
    public List<BlockPosition> dig(User user, BlockPosition pos) {
		PlayerConnection con = user.getConnection();
		animate(con, pos.west().west());
        animate(con, pos.south().south());
        animate(con, pos.east().east());
        animate(con, pos.north().north());
        animate(con, pos.west().south());
        animate(con, pos.south().west());
        animate(con, pos.east().north());
        animate(con, pos.north().east());
        return null;
    }
}
