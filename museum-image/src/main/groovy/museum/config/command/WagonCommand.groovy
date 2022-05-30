@groovy.transform.BaseScript(museum.MuseumScript)
package museum.config.command

import clepto.bukkit.LocalArmorStand
import clepto.bukkit.routine.Do
import museum.App
import museum.client_conversation.AnimationUtil
import museum.museum.Museum
import museum.museum.map.SubjectType
import museum.util.MessageUtil
import net.minecraft.server.v1_12_R1.EnumItemSlot
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer
import org.bukkit.event.EventPriority
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.inventory.ItemStack

class WagonConfig {
    static def COST = 500
}

def playerOrderedWagon = new ArrayList<UUID>()

Do.every 1 ticks {
    for (user in App.app.users) {
        if (!user.grabbedArmorstand)
            continue
        Location eyes = user.eyes
        user.grabbedArmorstand.setLocation eyes.add((eyes.direction * 1.5)).subtract(0, 1.7, 0), false
        user.grabbedArmorstand.handle.yaw = user.location.yaw
    }
}

on PlayerQuitEvent, EventPriority.LOWEST, {
    if (playerOrderedWagon.contains(player.uniqueId)) {
        def user = App.app.getUser player
        user.giveMoney(WagonConfig.COST)
        playerOrderedWagon.remove player.uniqueId
    }
}

registerCommand 'wagon' handle {
    def user = App.app.getUser sender as CraftPlayer
    if (playerOrderedWagon.contains user.uuid) {
        if ((292 - user.location.x)**2 + (87 - user.location.y)**2 + (-400 - user.location.z)**2 > 25)
            return
        // Выдача коробки игроку
        def stand = new LocalArmorStand(user.player)
        stand.equip EnumItemSlot.HEAD, new ItemStack(Material.CHEST)
        stand.setName 'Груз'
        stand.setLocation user.location
        stand.spawn()
        stand.handle.invisible = true
        stand.handle.headPose = [0, 180, 0]

        user.grabbedArmorstand = stand
        user.player.allowFlight = false
        user.player.walkSpeed = 0.08
        user.player.flying = false

        playerOrderedWagon.remove user.uuid
        return MessageUtil.get('box-taken')
    }
}

registerCommand 'wagonbuy' handle {
    def user = App.app.getUser sender as CraftPlayer
    if (user.getMoney() < WagonConfig.COST) {
        AnimationUtil.buyFailure(user)
        return null
    }
    if (playerOrderedWagon.contains user.uuid)
        return MessageUtil.get('wagon-copy')
    user.giveMoney(-WagonConfig.COST)
    playerOrderedWagon.add user.uuid
    return MessageUtil.get('wagon-buy')
}

registerCommand 'go' handle {
    def user = App.app.getUser player.uniqueId
    def state = user.state
    if (state instanceof Museum && !user.grabbedArmorstand) {
        for (stall in state.getSubjects(SubjectType.STALL)) {
            if (stall.food.isEmpty() && stall.allocation) {
                user.teleport(stall.allocation.clone().substruct(0.0, 0.0, 4.0))
                user.location.yaw = 0
                user.location.pitch = 0
                return
            }
        }
    }
    return
}
