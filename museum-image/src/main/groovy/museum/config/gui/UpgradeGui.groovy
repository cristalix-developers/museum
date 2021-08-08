package museum.config.gui

import museum.App
import museum.client_conversation.AnimationUtil
import museum.util.MessageUtil
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player

import static clepto.bukkit.menu.Guis.register
import static org.bukkit.Material.*

register 'tools', { player ->
    title 'Меню инструментов'
    layout '---F-S--X'

    button MuseumGuis.background

    button 'F' icon {
        item GOLD_PICKAXE
        text """
        §bКирки

        Приобретите новую кирку,
        и разгадайте тайны песка...
        """
        nbt.HideFlags = 63
    } leftClick {
        performCommand 'gui pickaxe'
    }
    button 'S' icon {
        item FISHING_ROD
        text """
        §bКрюк

        Улучшайте крюк, чтобы
        быстрее получать опыт на
        реке международных раскопок
        кристаллов.
        """
        nbt.HideFlags = 63
    } leftClick {
        performCommand 'gui rod'
    }
    button 'X' icon {
        item BARRIER
        text '§cНазад'
    } leftClick {
        performCommand("gui main")
    }
}

register 'rod', { player ->
    def user = App.app.getUser((Player) player)
    double cost
    switch (user.info.hookLevel - 1) {
        case 0: cost = 30000; break
        case 1: cost = 1000000; break
        case 2: cost = 3000000; break
        case 3: cost = 10000000; break
    }

    title 'Улучшение крюка'
    layout '----S---X'
    button MuseumGuis.background
    button 'S' icon {
        if (user.info.hookLevel > 3) {
            item CLAY_BALL
            nbt.other = 'tochka'
            text.clear()
            text '§8У вас наилучший крюк.'
        } else {
            item FISHING_ROD
            enchant Enchantment.LURE, user.info.hookLevel
            text """
            &eКрюк УР. ${user.info.hookLevel + 1} 

            Купить за &e${MessageUtil.toMoneyFormat(cost)}
            """
            nbt.Unbreakable = 1
        }
    } leftClick {
        if (user.money < cost) {
            AnimationUtil.buyFailure(user)
            return MessageUtil.get('nomoney')
        }
        user.giveMoney(-cost)
        user.info.hookLevel = user.info.hookLevel + 1
        closeInventory()
        return MessageUtil.find('buy-hook').set('level', user.info.hookLevel).text
    }

    button 'X' icon {
        item BARRIER
        text '§cНазад'
    } leftClick {
        performCommand("gui tools")
    }
}