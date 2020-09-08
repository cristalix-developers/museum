package museum.listener;

import lombok.AllArgsConstructor;
import lombok.val;
import museum.App;
import museum.util.MessageUtil;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.EquipmentSlot;

/**
 * @author func 08.06.2020
 * @project Museum
 */
@AllArgsConstructor
public class MuseumEventHandler implements Listener {

	private final App app;

	@EventHandler
	public void onInteract(PlayerInteractEvent e) {
		if (e.getHand() == EquipmentSlot.OFF_HAND || (e.getAction() != Action.RIGHT_CLICK_AIR && e.getAction() != Action.RIGHT_CLICK_BLOCK))
			return;
		val player = e.getPlayer();
		val itemStack = player.getInventory().getItemInMainHand();

		if (itemStack == null || itemStack.getItemMeta() == null)
			return;

		val type = itemStack.getType();
		if (type == Material.PAPER)
			player.performCommand("gui main");
		else if (type == Material.SADDLE)
			player.performCommand("home");
		else {
			player.getInventory().forEach(item -> {
				if (item == null || item.getItemMeta() == null)
					return;

				if (item.getType() == Material.EMERALD) {
					val user = app.getUser(player);
					user.setMoney(user.getMoney() + 200);
					MessageUtil.find("emerald").send(user);
					item.setAmount(item.getAmount() - 1);
				}
			});
		}
	}

	@EventHandler
	public void onMove(PlayerMoveEvent e) {
		val from = e.getFrom();
		val to = e.getTo();

		if (from.getBlockX() == to.getBlockX() && from.getBlockY() == to.getBlockY() && from.getBlockZ() == to.getBlockZ())
			return;

		val player = e.getPlayer();
		val user = app.getUser(player.getUniqueId());

		if (user.getExcavation() != null || user.getCoins() == null)
			return;

		// Попытка скушать монетки
		user.getCoins().removeIf(coin -> coin.pickUp(user, to, 1.7, player.getEntityId()));
	}

}
