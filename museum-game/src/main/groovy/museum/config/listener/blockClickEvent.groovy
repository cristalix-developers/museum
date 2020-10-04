package museum.config.listener

import org.bukkit.Material
import org.bukkit.entity.Bat
import org.bukkit.entity.EntityType
import org.bukkit.entity.LivingEntity
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

import static clepto.bukkit.behaviour.Behaviour.*

def invisible = new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 1, false, false)

on PlayerInteractEvent, {
    if (!blockClicked)
        return
    def location = clickedBlock.location

    if (clickedBlock.type == Material.PISTON_EXTENSION) {
        Bat bat = location.world.spawnEntity(location.clone().add(0.5, 0.2, 0.5), EntityType.BAT) as Bat
        bat.setAI false
        bat.addPotionEffect invisible
        bat.addPassenger player as LivingEntity
    }
    return
}
