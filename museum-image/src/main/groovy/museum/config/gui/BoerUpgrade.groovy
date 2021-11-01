package museum.config.gui

import implario.humanize.Humanize
import museum.client_conversation.AnimationUtil
import museum.cosmos.boer.Boer
import museum.cosmos.boer.BoerType
import museum.util.MessageUtil
import org.bukkit.entity.Player

import static clepto.bukkit.menu.Guis.register
import static museum.App.getApp
import static org.bukkit.Material.CLAY_BALL

register 'boer-upgrade', { player ->
    def user = app.getUser((Player) player)

    Boer boer
    try {
        boer = context as Boer
    } catch (Exception ignored) {
        return null
    }

    if (boer == null)
        return null

    title 'Настройка бура'

    layout '-D-B-R--C'

    button 'D' icon {
        item CLAY_BALL
        text ((boer.notification ? '&cОтключить' : '&aВключить') + ' уведомления')
        nbt.other = 'info1'
    } leftClick {
        boer.notification = !boer.notification
        closeInventory()
    }

    button 'B' icon {
        item boer.item.type
        text "&b${boer.type.address} бур"
        if (boer.type != BoerType.PRESTIGIOUS) {
            def now = boer.type
            def next = boer.type.next

            def nowHours = now.time / 3600 as int
            def nextHours = next.time / 3600 as int

            text """            

            &7Стоимость &a${MessageUtil.toMoneyFormat(next.price)}

            &b${boer.type.ordinal() + 1} &fуровень ➠ &b&l${boer.type.ordinal() + 2} уровень &a▲▲▲

            &7Время работы &f${nowHours} &7${Humanize.plurals('час', 'часа', 'часов', nowHours)}&f ➠ &b${nextHours} &7${Humanize.plurals('час', 'часа', 'часов', nextHours)}
            &7Скорость добычи &f${now.speed} &7сек.&f ➠ &b${next.speed} &7сек.
            """
        } else {
            text "&aБур максимального уровня"
        }
        nbt.other = 'guild_members_add'
    } leftClick {
        if (boer.type.next != null && user.money >= boer.type.next.price) {
            AnimationUtil.glowing(user, 0, 0, 100)
            user.giveMoney(-boer.type.next.price)
            boer.type = boer.type.next
            if (!boer.stands.isEmpty())
                boer.stands.get(5).helmet = boer.type.block.asBukkitMirror()
            closeInventory()
        } else {
            AnimationUtil.buyFailure user
        }
    }

    button 'R' icon {
        item CLAY_BALL
        text '§cУбрать бур'
        nbt.other = 'guild_members_remove'
    } leftClick {
        if (!boer.stands.isEmpty())
            boer.boerRemove()
        closeInventory()
    }

    button 'C' icon {
        item CLAY_BALL
        text '§cНазад'
        nbt.other = 'cancel'
    } leftClick {
        closeInventory()
    }
}