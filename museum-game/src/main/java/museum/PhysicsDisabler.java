package museum;

import clepto.bukkit.B;
import com.destroystokyo.paper.event.player.PlayerAdvancementCriterionGrantEvent;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.FallingBlock;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.*;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.player.*;
import org.spigotmc.event.entity.EntityDismountEvent;

import static org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason.NATURAL;

public class PhysicsDisabler implements Listener {

	@EventHandler
	public void disable(PlayerInteractEntityEvent event) {
		event.setCancelled(true);
	}

	@EventHandler
	public void disable(PlayerInteractEvent event) {
		if (event.action == Action.RIGHT_CLICK_BLOCK)
			event.setCancelled(true);
	}

	@EventHandler
	public void disable(EntityDismountEvent event) {
		if (event.dismounted.getType() == EntityType.BAT)
			B.postpone(1, () -> event.dismounted.remove());
	}

	@EventHandler
	public void disable(EntityChangeBlockEvent event) {
		if (event.entity instanceof FallingBlock)
			event.setCancelled(true);
	}

	@EventHandler
	public void disable(EntityDamageEvent event) {
		event.setCancelled(true);
	}

	@EventHandler
	public void disable(BlockBreakEvent event) {
		event.setCancelled(true);
	}

	@EventHandler
	public void disable(FoodLevelChangeEvent event) {
		event.setFoodLevel(20);
	}

	@EventHandler
	public void disable(BlockFadeEvent event) {
		event.setCancelled(true);
	}

	@EventHandler
	public void disable(BlockPhysicsEvent event) {
		event.setCancelled(true);
	}

	@EventHandler
	public void disable(BlockSpreadEvent event) {
		event.setCancelled(true);
	}

	@EventHandler
	public void disable(BlockGrowEvent event) {
		event.setCancelled(true);
	}

	@EventHandler
	public void disable(BlockFromToEvent event) {
		event.setCancelled(true);
	}

	@EventHandler
	public void disable(PlayerDropItemEvent event) {
		event.setCancelled(true);
	}

	@EventHandler
	public void disable(HangingBreakByEntityEvent event) {
		event.setCancelled(true);
	}

	@EventHandler
	public void disable(BlockBurnEvent event) {
		event.setCancelled(true);
	}

	@EventHandler
	public void disable(EntityExplodeEvent event) {
		event.setCancelled(true);
	}

	@EventHandler
	public void disable(PlayerArmorStandManipulateEvent event) {
		event.setCancelled(true);
	}

	@EventHandler
	public void disable(CreatureSpawnEvent event) {
		event.setCancelled(event.spawnReason == NATURAL);
	}

	@EventHandler
	public void disable(PlayerAdvancementCriterionGrantEvent event) {
		event.setCancelled(true);
	}

	@EventHandler
	public void disable(PlayerSwapHandItemsEvent event) {
		event.setCancelled(true);
	}

}
