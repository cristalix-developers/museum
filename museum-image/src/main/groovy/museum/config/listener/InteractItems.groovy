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

register 'sugar-treasure', {
    item SUGAR
    nbt.cost = 120
    text """
        §eСтранный белый песок §6+120\$

        Хм... Что же это, мы уже
        второй раз натыкаемся на него,
        пока ученые не раскрыли его тайну...
    """
}

register 'diamond-treasure', {
    item DIAMOND
    nbt.cost = 134
    text """
        §eДрагоценный минерал §6+134\$

        Иногда его можно найти
        на раскопках, стоит очень много!
    """
}

register 'ghost-treasure', {
    item GHAST_TEAR
    nbt.cost = 67
    text """
        §eСлеза Боба §6+67\$

        Ходят слухи, что она
        вообще не из этого мира...
    """
}

register 'arrow-treasure', {
    item ARROW
    nbt.cost = 46
    text """
        §eПервобытная стрела §6+46\$

        Откуда она тут?!
        Это еще предстоит узнать нашим
        ученым из музея...
    """
}

register 'emerald-treasure', {
    item EMERALD
    nbt.cost = 36
    text """
        §eДрагоценный камень §6+36\$

        Иногда его можно найти
        на раскопках, стоит не много.
    """
}

register 'flint-treasure', {
    item FLINT
    nbt.cost = 74
    text """
        §eПервобытное орудие §6+74\$

        Иногда его можно найти
        на раскопках, стоит не много.
    """
}

register 'sink-treasure', {
    item CLAY_BALL
    nbt.museum = 'sink'
    nbt.cost = 225
    text """
        §eДревняя раковина §6+225\$

        Древняя раковина, свидетельствует
        о мире до появления млекопитающих...
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
