package ru.func.museum.player.pickaxe;

import net.minecraft.server.v1_12_R1.BlockPosition;
import net.minecraft.server.v1_12_R1.PlayerConnection;
import ru.func.museum.excavation.Excavation;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author func 24.05.2020
 * @project Museum
 */
public class PrestigePickaxe implements Pickaxe {

    @Override
    public List<BlockPosition> dig(PlayerConnection connection, Excavation excavation, BlockPosition blockPosition) {
        return Stream.of(
                blockPosition.east().east(),
                blockPosition.west().west(),
                blockPosition.south().south(),
                blockPosition.north().north()
        ).filter(position -> breakBlock(connection, excavation, position)).collect(Collectors.toList());
    }
}
