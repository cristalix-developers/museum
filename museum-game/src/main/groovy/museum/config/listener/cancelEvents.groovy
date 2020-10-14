@groovy.transform.BaseScript(museum.MuseumScript)
package museum.config.listener

import com.destroystokyo.paper.event.player.PlayerAdvancementCriterionGrantEvent
import org.bukkit.entity.EntityType
import org.bukkit.entity.FallingBlock
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockBurnEvent
import org.bukkit.event.block.BlockFromToEvent
import org.bukkit.event.block.BlockPhysicsEvent
import org.bukkit.event.entity.*
import org.bukkit.event.hanging.HangingBreakByEntityEvent
import org.bukkit.event.player.PlayerArmorStandManipulateEvent
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.event.player.PlayerInteractEntityEvent
import org.bukkit.event.player.PlayerSwapHandItemsEvent
import org.spigotmc.event.entity.EntityDismountEvent

on EntityDismountEvent, {
    if (dismounted.type == EntityType.BAT)
        dismounted.remove()
}

on EntityChangeBlockEvent, {
    if(entity instanceof FallingBlock)
        setCancelled true
}

on EntityDamageEvent, {
    setCancelled true
}

on PlayerArmorStandManipulateEvent, {
    setCancelled true
}

on BlockBreakEvent, {
    setCancelled true
}

on FoodLevelChangeEvent, {
    setFoodLevel 20
}

on BlockPhysicsEvent, {
    setCancelled true
}

on BlockFromToEvent, {
    setCancelled true
}

on PlayerInteractEntityEvent, {
    setCancelled true
}

on PlayerDropItemEvent, {
    setCancelled true
}

on HangingBreakByEntityEvent, {
    setCancelled true
}

on BlockBurnEvent, {
    setCancelled true
}

on EntityExplodeEvent, {
    setCancelled true
}

on PlayerArmorStandManipulateEvent, {
    setCancelled true
}

on CreatureSpawnEvent, {
    setCancelled spawnReason == CreatureSpawnEvent.SpawnReason.NATURAL
}

on PlayerAdvancementCriterionGrantEvent, {
    setCancelled true
}

on PlayerSwapHandItemsEvent, {
    setCancelled true
}
