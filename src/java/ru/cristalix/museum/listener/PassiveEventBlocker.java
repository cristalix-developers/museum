package ru.cristalix.museum.listener;

import com.destroystokyo.paper.event.player.PlayerAdvancementCriterionGrantEvent;
import org.bukkit.entity.FallingBlock;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.*;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;

/**
 * @author func 04.06.2020
 * @project Museum
 */
public class PassiveEventBlocker implements Listener {

	@EventHandler
	public void onBlockChange(EntityChangeBlockEvent event){
		if(event.getEntity() instanceof FallingBlock)
			event.setCancelled(true);
	}

	@EventHandler
	public void onDamage(EntityDamageEvent e) {
		e.setCancelled(true);
	}

	@EventHandler
	public void onArmorstandManipulation(PlayerArmorStandManipulateEvent event) {
		event.setCancelled(true);
	}

	@EventHandler
	public void onBlockPlace(BlockPlaceEvent event) {
		event.setCancelled(true);
	}

	@EventHandler
	public void onBlockBreak(BlockBreakEvent event) {
		event.setCancelled(true);
	}

	@EventHandler
	public void onFooLevenChange(FoodLevelChangeEvent event) {
		event.setFoodLevel(20);
	}

	@EventHandler
	public void onPhysics(BlockPhysicsEvent event) {
		event.setCancelled(true);
	}

	@EventHandler
	public void onFromTo(BlockFromToEvent event) {
		event.setCancelled(true);
	}

	@EventHandler
	public void onVillangerClick(PlayerInteractEntityEvent event) {
		event.setCancelled(true);
	}

	@EventHandler
	public void onItemDrop(PlayerDropItemEvent event) {
		event.setCancelled(true);
	}

	@EventHandler
	public void onHangingBreak(HangingBreakByEntityEvent event) {
		event.setCancelled(true);
	}

	@EventHandler
	public void onBurn(BlockBurnEvent event) {
		event.setCancelled(true);
	}

	@EventHandler
	public void onExplode(EntityExplodeEvent event) {
		event.blockList().clear();
	}

	@EventHandler
	public void onArmorStand(PlayerArmorStandManipulateEvent event) {
		event.setCancelled(true);
	}

	@EventHandler
	public void onCreatureSpawn(CreatureSpawnEvent event) {
		event.setCancelled(event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.NATURAL);
	}

	@EventHandler
	public void onAchievement(PlayerAdvancementCriterionGrantEvent event) {
		event.setCancelled(true);
	}

	@EventHandler
	public void onSwap(PlayerSwapHandItemsEvent event) {
		event.setCancelled(true);
	}

}
