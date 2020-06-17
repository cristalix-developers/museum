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
public class DefaultPickaxe implements Pickaxe {

    @Override
    public List<BlockPosition> dig(PlayerConnection connection, Excavation excavation, BlockPosition blockPosition) {
        return Stream.of(
                blockPosition.east(),
                blockPosition.west(),
                blockPosition.south(),
                blockPosition.north(),
                blockPosition.down()
        ).filter(position -> breakBlock(connection, excavation, position)).collect(Collectors.toList());
    }
}
