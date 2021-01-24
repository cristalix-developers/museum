package museum.misc;

import clepto.bukkit.item.Items;
import lombok.Getter;
import lombok.val;
import museum.player.User;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

/**
 * @author func 11.11.2020
 * @project museum
 */
@Getter
public class Relic {

	private final String prototypeAddress;
	private final ItemStack relic;
	private final int price;
	private final UUID uuid = UUID.randomUUID();

	public Relic(String prototypeAddress) {
		this.prototypeAddress = prototypeAddress;
		val item = Items.render("relic-" + prototypeAddress);
		item.tag.setString("relic-uuid", uuid.toString());
		this.relic = item.asBukkitMirror();
		this.price = item.tag.getInt("price");
	}

	public void give(User user) {
		user.getPlayer().getInventory().addItem(relic);
		user.getRelics().add(this);
	}
}