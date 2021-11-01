package museum.cosmos.boer;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;

@Getter
@AllArgsConstructor
public enum BoerType {

    STANDARD("Стандартный", 30, 3600, 0, CraftItemStack.asNMSCopy(new ItemStack(Material.IRON_BLOCK))),
    PROFESSIONAL("Профессиональный", 25, 7200, 1000000, CraftItemStack.asNMSCopy(new ItemStack(Material.GOLD_BLOCK))),
    PRESTIGIOUS("Престижный", 10, 21600, 1000000000, CraftItemStack.asNMSCopy(new ItemStack(Material.DIAMOND_BLOCK))),
    ;

    private final String address;
    @Setter
    private int speed;
    @Setter
    private int time;
    private final double price;
    private final net.minecraft.server.v1_12_R1.ItemStack block;

    public BoerType getNext() {
        return ordinal() >= BoerType.values().length - 1 ? null : BoerType.values()[ordinal() + 1];
    }
}
