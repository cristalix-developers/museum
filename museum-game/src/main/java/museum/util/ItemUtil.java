package museum.util;

import clepto.bukkit.item.ItemBuilder;
import lombok.val;
import museum.player.User;
import net.minecraft.server.v1_12_R1.NBTTagCompound;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import ru.cristalix.core.permissions.IPermissionService;

public class ItemUtil {

    private static final IPermissionService permissionService = IPermissionService.get();

    public static ItemStack cosmoCrystal() {
        ItemStack cosmoCrystal = new ItemStack(Material.CLAY_BALL);
        val nmsItem = CraftItemStack.asNMSCopy(cosmoCrystal);
        nmsItem.tag = new NBTTagCompound();
        nmsItem.tag.setString("simulators", "save_crystal");
        return nmsItem.asBukkitMirror();
    }

    public static ItemStack getPlayerSkull(User user) {
        return new ItemBuilder()
                .item(Material.CLAY_BALL)
                .text("§bПерсонализация §f§lПКМ")
                .nbt("other", "stats")
                .build()
                .asBukkitMirror();
    }

    public static ItemStack getAgreeItem() {
        ItemStack agree = new ItemStack(Material.CLAY_BALL);
        val nmsItem = CraftItemStack.asNMSCopy(agree);
        nmsItem.tag = new NBTTagCompound();
        nmsItem.tag.setString("other", "access");
        return nmsItem.asBukkitMirror();
    }
}
