package museum.fragment;

import museum.player.User;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public interface Fragment {

	String getAddress();

	ItemStack getItem();

	UUID getUuid();

	int getPrice();

	default void give(User user) {
		user.getPlayer().getInventory().addItem(getItem());
		user.getRelics().add(this);
	}

	default void remove(User user) {
		user.getRelics().remove(this);
	}
}
