@groovy.transform.BaseScript(museum.MuseumScript)
package museum.config.gui


import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack
import org.bukkit.event.player.PlayerInteractEvent
import static org.bukkit.Material.CLAY_BALL

on PlayerInteractEvent, {
    if (item && item.type == CLAY_BALL) {
        def nmsItem = CraftItemStack.asNMSCopy(item)
        if (nmsItem.tag && nmsItem.tag.hasKeyOfType('other', 8))
            if (nmsItem.tag.getString('other') == 'achievements_lock')
                player.performCommand "achievements"
    }
}
