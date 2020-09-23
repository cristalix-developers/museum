package museum.config


import museum.util.MessageUtil
import org.bukkit.event.player.PlayerInteractEvent

import static clepto.bukkit.behaviour.Behaviour.on
import static museum.App.app
import static org.bukkit.Material.EMERALD

on PlayerInteractEvent, {
    def user = app.getUser player
    player.inventory.each {
        if (it && it.type == EMERALD) {
            user.money = user.money + 36
            it.amount = it.amount - 1
            MessageUtil.find "emerald" send user
        }
    }
}
