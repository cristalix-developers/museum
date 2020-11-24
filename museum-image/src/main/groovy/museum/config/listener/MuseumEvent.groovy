@groovy.transform.BaseScript(museum.MuseumScript)
package museum.config.listener

import museum.App
import museum.misc.PlacesMechanic
import museum.museum.Museum
import museum.museum.map.SubjectType
import museum.museum.subject.product.FoodProduct
import museum.util.MessageUtil
import org.bukkit.event.inventory.InventoryOpenEvent
import org.bukkit.event.inventory.InventoryType
import org.bukkit.event.player.AsyncPlayerChatEvent
import org.bukkit.event.player.PlayerMoveEvent
import ru.cristalix.core.math.V3

on InventoryOpenEvent, {
    def type = inventory.type
    cancelled = type == InventoryType.SHULKER_BOX || type == InventoryType.FURNACE
}

on AsyncPlayerChatEvent, {
    if (message.contains("visit") || message.contains("museum visit") || message.contains("посетите мой")) {
        player.sendMessage MessageUtil.get("no-spam")
        setCancelled true
    }
}

on PlayerMoveEvent, {
    if (from.blockX == to.blockX && from.blockY == to.blockY && from.blockZ == to.blockZ)
        return

    def user = App.app.getUser(player.uniqueId)

    if (!user || !user.state)
        return

    PlacesMechanic.handleMove user, new V3(to.x, to.y, to.z)

    if (user.state instanceof Museum) {
        def museum = user.state as Museum
        if (museum.owner != user)
            return
        // Попытка скушать монетки
        museum.coins.removeIf(coin -> coin.pickUp(user, to, 1.7, player.entityId))
        if (user.grabbedArmorstand) {
            // Попытка снять груз возле лавки
            museum.getSubjects(SubjectType.STALL).forEach(stall -> {
                // Если игрок находится к лавке в радиусе 5 блоков
                if (stall.allocation.origin.distanceSquared(to) < 55) {
                    user.grabbedArmorstand.remove()
                    user.grabbedArmorstand = null
                    user.player.allowFlight = true
                    user.player.walkSpeed = 0.33
                    MessageUtil.find 'box-recieved' send user
                    def summary = 0
                    8.times {
                        def food = FoodProduct.values()[(Math.random() * FoodProduct.values().length) as int]
                        stall.food.computeIfPresent food, (product, count) -> ++count
                        if (!stall.food.containsKey(food))
                            stall.food.put food, 1
                        summary = summary + food.cost
                        user.sendMessage(" x $food.name | §a$food.cost\$")
                    }
                    user.sendMessage("§e§lИтого: $summary\$")
                }
            })
        } else if ((292 - to.x)**2 + (87 - to.y)**2 + (-400 - to.z)**2 < 25) {
            user.performCommand 'wagon'
        }
    }
}
