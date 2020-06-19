package ru.cristalix.museum.player.pickaxe;

import net.minecraft.server.v1_12_R1.BlockPosition;
import ru.cristalix.museum.excavation.ExcavationManager;
import ru.cristalix.museum.player.User;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author func 24.05.2020
 * @project Museum
 */
public class PrestigePickaxe implements Pickaxe {

    @Override
    public List<BlockPosition> dig(User user, BlockPosition pos) {
        return Stream.of(
                pos.east().east(),
                pos.west().west(),
                pos.south().south(),
                pos.north().north()
        ).filter(p -> ExcavationManager.isAir(user, p)).collect(Collectors.toList());
    }
}
