@groovy.transform.BaseScript(museum.MuseumScript)
package museum.config.listener

import clepto.bukkit.menu.Guis
import museum.excavation.Excavation
import museum.util.TreasureUtil
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent

import static clepto.bukkit.item.Items.register
import static museum.App.app
import static org.bukkit.Material.*

on PAPER use {
    Guis.open player, 'main', null
}

on WOOD_DOOR use {
    player.performCommand 'gui visitor'
}

on SADDLE use {
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
    TreasureUtil.sellAll(user)
}
