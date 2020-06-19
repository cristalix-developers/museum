package ru.cristalix.museum.museum;

import lombok.AllArgsConstructor;
import lombok.val;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.EquipmentSlot;
import ru.cristalix.museum.App;

/**
 * @author func 08.06.2020
 * @project Museum
 */
@AllArgsConstructor
public class MuseumEvents implements Listener {

	private final App app;

	@EventHandler
	public void onInteract(PlayerInteractEvent e) {
		if (e.getHand() == EquipmentSlot.OFF_HAND) return;
		if (e.getAction() != Action.RIGHT_CLICK_AIR && e.getAction() != Action.RIGHT_CLICK_BLOCK) return;
		Player player = e.getPlayer();
		Material type = player.getInventory().getItemInMainHand().getType();
		if (type == Material.PAPER) player.performCommand("gui main");
		else if (type == Material.EMERALD) player.performCommand("gui pickaxes");
	}

	@EventHandler
	public void onMove(PlayerMoveEvent e) {
		val from = e.getFrom();
		val to = e.getTo();

		if (from.getBlockX() == to.getBlockX() && from.getBlockY() == to.getBlockY() && from.getBlockZ() == to.getBlockZ()) return;

		val player = e.getPlayer();
		val user = app.getUser(player.getUniqueId());

		if (user.getExcavation() != null) return;

		// Попытка скушать монетки
		user.getCoins().removeIf(coin -> coin.pickUp(user, to, 1.7));
	}

}
