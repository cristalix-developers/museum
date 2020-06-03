package ru.func.museum.player.pickaxe;

import lombok.val;
import net.minecraft.server.v1_12_R1.BlockPosition;
import net.minecraft.server.v1_12_R1.PlayerConnection;
import org.bukkit.Material;
import ru.cristalix.core.item.Items;
import ru.func.museum.excavation.Excavation;

import java.util.function.Supplier;

/**
 * @author func 24.05.2020
 * @project Museum
 */
public class UltraPickaxe implements Pickaxe {

    private Supplier<Items.Builder> template = () -> Items.builder().type(Material.DIAMOND_PICKAXE);

    @Override
    public Supplier<Items.Builder> getItem() {
        return template;
    }
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
