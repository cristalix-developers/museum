package ru.func.museum.player.pickaxe;

import net.minecraft.server.v1_12_R1.BlockPosition;
import net.minecraft.server.v1_12_R1.PlayerConnection;
import ru.func.museum.excavation.Excavation;

import java.util.List;

/**
 * @author func 24.05.2020
 * @project Museum
 */
public class ProfessionalPickaxe implements Pickaxe {

    @Override
    public List<BlockPosition> dig(PlayerConnection connection, Excavation excavation, BlockPosition blockPosition) {
        animate(connection, blockPosition.west().west());
        animate(connection, blockPosition.south().south());
        animate(connection, blockPosition.east().east());
        animate(connection, blockPosition.north().north());
        animate(connection, blockPosition.west().south());
        animate(connection, blockPosition.south().west());
        animate(connection, blockPosition.east().north());
        animate(connection, blockPosition.north().east());
        return null;
    }
}
