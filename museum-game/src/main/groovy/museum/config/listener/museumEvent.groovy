@groovy.transform.BaseScript(museum.MuseumScript)
package museum.config.listener

import museum.App
import museum.museum.Museum
import museum.museum.map.SubjectType
import org.bukkit.event.inventory.InventoryOpenEvent
import org.bukkit.event.inventory.InventoryType
import org.bukkit.event.player.PlayerMoveEvent

on InventoryOpenEvent, {
    def type = inventory.type
    cancelled = type == InventoryType.SHULKER_BOX || type == InventoryType.FURNACE
}

on PlayerMoveEvent, {
    if (from.blockX == to.blockX && from.blockY == to.blockY && from.blockZ == to.blockZ)
        return

    def user = App.app.getUser(player.uniqueId)

    if (user.state instanceof Museum) {
        def museum = user.state as Museum
        // Попытка скушать монетки
        museum.coins.removeIf(coin -> coin.pickUp(user, to, 1.7, player.entityId))
        // Попытка снять груз возле лавки
        museum.getSubjects(SubjectType.STALL).forEach(stall -> {
            // Если игрок находится к лавке в радиусе 10 блоков
            if (stall.allocation.origin.distanceSquared(to) < 100) {
                // todo: убрать груз, пополнить лавку
            }
        })
    }
}
