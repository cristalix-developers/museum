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

    STANDARD("Стандартный", 60, 3600, 0, CraftItemStack.asNMSCopy(new ItemStack(Material.IRON_BLOCK))),
    PROFESSIONAL("Профессиональный", 40, 3600 * 2, 1000000, CraftItemStack.asNMSCopy(new ItemStack(Material.GOLD_BLOCK))),
    PRESTIGIOUS("Престижный", 25, 3600 * 4, 1000000000, CraftItemStack.asNMSCopy(new ItemStack(Material.DIAMOND_BLOCK))),
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
