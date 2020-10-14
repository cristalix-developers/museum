@groovy.transform.BaseScript(museum.MuseumScript)
package museum.config.listener

import com.destroystokyo.paper.event.player.PlayerUseUnknownEntityEvent
import museum.App
import museum.worker.WorkerUtil
import org.bukkit.inventory.EquipmentSlot

on PlayerUseUnknownEntityEvent, {
    if (hand == EquipmentSlot.OFF_HAND)
        return
    WorkerUtil.acceptClick App.app.getUser(player), entityId
}