package museum.config.listener

import museum.App
import museum.museum.Museum
import org.bukkit.event.inventory.InventoryOpenEvent
import org.bukkit.event.inventory.InventoryType
import org.bukkit.event.player.PlayerMoveEvent

import static clepto.bukkit.behaviour.Behaviour.on

on InventoryOpenEvent, {
    def type = inventory.type
    cancelled = type == InventoryType.SHULKER_BOX || type == InventoryType.FURNACE
}

on PlayerMoveEvent, {
    if (from.blockX == to.blockX && from.blockY == to.blockY && from.blockZ == to.blockZ)
        return

    def user = App.app.getUser(player.uniqueId)

    // Попытка скушать монетки
    if (user.state instanceof Museum)
        (user.state as Museum).coins.removeIf(coin -> coin.pickUp(user, to, 1.7, player.entityId))
}
