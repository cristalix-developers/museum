package ru.func.museum.player.pickaxe;

import lombok.val;
import net.minecraft.server.v1_12_R1.BlockPosition;
import net.minecraft.server.v1_12_R1.PlayerConnection;
import ru.func.museum.excavation.Excavation;

/**
 * @author func 24.05.2020
 * @project Museum
 */
public class PrestigePickaxe implements Pickaxe {

    @Override
    public void dig(PlayerConnection connection, Excavation excavation, BlockPosition blockPosition) {
        for (val position : new BlockPosition[]{
                blockPosition.east(),
                blockPosition.north(),
                blockPosition.down(),
                blockPosition.south(),
                blockPosition.west()
        }) {
            if (RANDOM.nextInt(2) == 1)
                breakBlock(connection, excavation, position);
        }
    }
}
