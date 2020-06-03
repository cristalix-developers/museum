package ru.func.museum.player.pickaxe;

import net.minecraft.server.v1_12_R1.BlockPosition;
import net.minecraft.server.v1_12_R1.PacketPlayOutBlockBreakAnimation;
import net.minecraft.server.v1_12_R1.PlayerConnection;
import org.bukkit.Material;
import ru.cristalix.core.item.Items;
import ru.func.museum.excavation.Excavation;

import java.util.function.Supplier;

/**
 * @author func 24.05.2020
 * @project Museum
 */
public class RarePickaxe implements Pickaxe {

    private Supplier<Items.Builder> template = () -> Items.builder().type(Material.IRON_PICKAXE);

    @Override
    public Supplier<Items.Builder> getItem() {
        return template;
    }

    @Override
    public void dig(PlayerConnection connection, Excavation excavation, BlockPosition blockPosition) {
        for (BlockPosition position : new BlockPosition[]{
                blockPosition.east(),
                blockPosition.north(),
                blockPosition.down(),
                blockPosition.south(),
                blockPosition.west()
        }) {
            connection.sendPacket(new PacketPlayOutBlockBreakAnimation(
                    position.hashCode() + RANDOM.nextInt(11),
                    position,
                    4 + RANDOM.nextInt(5)
            ));
            if (RANDOM.nextInt(6) == 5)
                breakBlock(connection, excavation, position);
        }
    }
}
