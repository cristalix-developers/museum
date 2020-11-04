package museum.util;

import lombok.experimental.UtilityClass;
import lombok.val;
import museum.player.User;
import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;

import java.util.Optional;

/**
 * @author func 19.10.2020
 * @project museum
 */
@UtilityClass
public class TreasureUtil {

	private Optional<Double> getTreasurePrice(ItemStack item) {
		val tag = CraftItemStack.asNMSCopy(item).getTag();
		if (tag == null || !tag.hasKeyOfType("cost", 99))
			return Optional.empty();
		return Optional.of(tag.getDouble("cost"));
	}

	public void sellAll(User user) {
		for (val item : user.getInventory()) {
			if (item == null)
				continue;
			getTreasurePrice(item).ifPresent(price -> {
				user.depositMoneyWithBooster(price * item.getAmount());
				MessageUtil.find("treasure-item")
						.set("cost", price * item.getAmount())
						.send(user);
				item.setAmount(0);
			});
		}
	}
}