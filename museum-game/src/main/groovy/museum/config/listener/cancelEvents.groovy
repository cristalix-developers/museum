package museum.config.listener

import com.destroystokyo.paper.event.player.PlayerAdvancementCriterionGrantEvent
import org.bukkit.entity.EntityType
import org.bukkit.entity.FallingBlock
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockBurnEvent
import org.bukkit.event.block.BlockFromToEvent
import org.bukkit.event.block.BlockPhysicsEvent
import org.bukkit.event.entity.CreatureSpawnEvent
import org.bukkit.event.entity.EntityChangeBlockEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.EntityExplodeEvent
import org.bukkit.event.entity.FoodLevelChangeEvent
import org.bukkit.event.hanging.HangingBreakByEntityEvent
import org.bukkit.event.player.PlayerArmorStandManipulateEvent
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.event.player.PlayerInteractEntityEvent
import org.bukkit.event.player.PlayerSwapHandItemsEvent
import org.spigotmc.event.entity.EntityDismountEvent

import static clepto.bukkit.behaviour.Behaviour.*

on EntityDismountEvent, {
    if (dismounted.type == EntityType.BAT)
        dismounted.remove()
}

on EntityChangeBlockEvent, {
    if(entity instanceof FallingBlock)
        cancelled = true
}

on EntityDamageEvent, {
    cancelled = true
}

on PlayerArmorStandManipulateEvent, {
    cancelled = true
}

on BlockBreakEvent, {
    cancelled = true
}

on FoodLevelChangeEvent, {
    foodLevel = 20
}

on BlockPhysicsEvent, {
    cancelled = true
}

on BlockFromToEvent, {
    cancelled = true
}

on PlayerInteractEntityEvent, {
    cancelled = true
}

on PlayerDropItemEvent, {
    cancelled = true
}

on HangingBreakByEntityEvent, {
    cancelled = true
}

on BlockBurnEvent, {
    cancelled = true
}

on EntityExplodeEvent, {
    cancelled = true
}

on PlayerArmorStandManipulateEvent, {
    cancelled = true
}

on CreatureSpawnEvent, {
    cancelled = spawnReason == CreatureSpawnEvent.SpawnReason.NATURAL
}

on PlayerAdvancementCriterionGrantEvent, {
     cancelled = true
}

on PlayerSwapHandItemsEvent, {
    cancelled = true
}
