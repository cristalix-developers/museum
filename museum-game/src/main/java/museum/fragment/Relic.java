package museum.fragment;

import clepto.bukkit.item.Items;
import lombok.Getter;
import lombok.val;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

/**
 * @author func 11.11.2020
 * @project museum
 */
@Getter
public class Relic implements Fragment {

	private final String address;
	private final ItemStack item;
	private final int price;
	private final UUID uuid = UUID.randomUUID();

	public Relic(String prototypeAddress) {
		this.address = prototypeAddress;
		val item = Items.render("relic-" + prototypeAddress);
		item.tag.setString("relic-uuid", uuid.toString());
		this.item = item.asBukkitMirror();
		this.price = item.tag.getInt("price");
	}
}