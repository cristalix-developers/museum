package museum.listener;

import lombok.AllArgsConstructor;
import lombok.val;
import museum.App;
import museum.museum.Museum;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerMoveEvent;

/**
 * @author func 08.06.2020
 * @project Museum
 */
@AllArgsConstructor
public class MuseumEventHandler implements Listener {

	private final App app;

	@EventHandler
	public void onOpenInventory(InventoryOpenEvent event) {
		val type = event.getInventory().getType();
		event.setCancelled(type == InventoryType.SHULKER_BOX || type == InventoryType.FURNACE);
	}

	@EventHandler
	public void onMove(PlayerMoveEvent event) {
		val from = event.getFrom();
		val to = event.getTo();

		if (from.getBlockX() == to.getBlockX() && from.getBlockY() == to.getBlockY() && from.getBlockZ() == to.getBlockZ())
			return;

		val player = event.getPlayer();
		val user = app.getUser(player.getUniqueId());

		if (!(user.getState() instanceof Museum))
			return;

		// Попытка скушать монетки
		((Museum) user.getState()).getCoins().removeIf(coin -> coin.pickUp(user, to, 1.7, player.getEntityId()));
	}

}
