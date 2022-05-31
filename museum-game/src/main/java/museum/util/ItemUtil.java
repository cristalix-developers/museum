package museum.util;

import lombok.val;
import museum.player.User;
import net.minecraft.server.v1_12_R1.NBTTagCompound;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

public class ItemUtil {

    public static ItemStack cosmoCrystal() {
        ItemStack cosmoCrystal = new ItemStack(Material.CLAY_BALL);
        val nmsItem = CraftItemStack.asNMSCopy(cosmoCrystal);
        nmsItem.tag = new NBTTagCompound();
        nmsItem.tag.setString("simulators", "save_crystal");
        return nmsItem.asBukkitMirror();
    }

    public static ItemStack getPlayerSkull(User user) {
        ItemStack skull = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
        SkullMeta meta = (SkullMeta) skull.getItemMeta();
        meta.setPlayerProfile(user.getPlayer().getPlayerProfile());
        skull.setItemMeta(meta);
        return skull;
    }
}
