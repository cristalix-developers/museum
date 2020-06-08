package ru.func.museum.museum.coin;

import net.minecraft.server.v1_12_R1.ItemStack;
import net.minecraft.server.v1_12_R1.PlayerConnection;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack;
import ru.cristalix.core.item.Items;
import ru.func.museum.player.Archaeologist;

public interface AbstractCoin {

    ItemStack COIN = CraftItemStack.asNMSCopy(Items.builder()
            .type(Material.DOUBLE_PLANT)
            .displayName("Монета")
            .build()
    );

    void remove(PlayerConnection connection);

    void create(PlayerConnection connection);

    boolean pickUp(PlayerConnection connection, Archaeologist archaeologist, Location location, double radius);
}
