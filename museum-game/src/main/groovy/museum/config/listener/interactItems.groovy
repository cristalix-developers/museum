package museum.config.listener

import clepto.bukkit.menu.Guis
import museum.excavation.Excavation
import museum.util.MessageUtil
import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent

import static clepto.bukkit.behaviour.Behaviour.on
import static clepto.bukkit.behaviour.Behaviour.when
import static clepto.bukkit.item.Items.register
import static museum.App.app
import static org.bukkit.Material.*

when PAPER used {
    Guis.open player, 'main', null
}

when WOOD_DOOR used {
    player.performCommand 'gui visitor'
}

when SADDLE used {
    player.performCommand 'home'
}

register 'emerald-treasure', {
    item EMERALD
    nbt.cost = 36
    text """
        §aДрагоценный камень §6+36\$

        Иногда его можно найти
        на раскопках, стоит не много.
    """
}

register 'flint-treasure', {
    item FLINT
    nbt.cost = 74
    text """
        §aПервобытное орудие §6+74\$

        Иногда его можно найти
        на раскопках, стоит не много.
    """
}

on PlayerInteractEvent, {
    if (action != Action.RIGHT_CLICK_BLOCK && action != Action.RIGHT_CLICK_AIR)
        return
    def user = app.getUser player
    if (!(user.getState() instanceof Excavation))
        return
    for (item in player.inventory) {
        if (!item)
            continue
        def tag = CraftItemStack.asNMSCopy(item).getTag()
        if (!tag || !tag.hasKeyOfType('cost', 99))
            continue
        def cost = tag.getInt('cost')
        item.amount = item.amount - 1
        user.money = user.money + cost
        MessageUtil.find "treasure-item" set 'cost', cost send user
        return
    }
}
