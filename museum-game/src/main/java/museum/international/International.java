package museum.international;

import clepto.bukkit.item.Items;
import museum.player.State;
import museum.player.User;
import net.minecraft.server.v1_12_R1.PacketPlayInBlockDig;
import org.bukkit.inventory.ItemStack;

public interface International extends State {

	ItemStack BACK_ITEM = Items.render("goback").asBukkitMirror();

	void acceptBlockBreak(User user, PacketPlayInBlockDig packet);

}
