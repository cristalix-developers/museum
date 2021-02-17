package museum.config.gui

import clepto.bukkit.item.Items
import clepto.bukkit.menu.Guis
import museum.App
import museum.fragment.GemType
import museum.prototype.Managers
import museum.util.LevelSystem
import org.apache.commons.lang.StringUtils
import org.bukkit.entity.Player

import java.text.DecimalFormat

import static org.bukkit.Material.*

def moneyFormatter = new DecimalFormat('###,###,###,###,###,###.##$')

Guis.register 'excavation', { player ->
    def user = App.app.getUser((Player) player)

    def realDay = GemType.getActualGem().ordinal() + 1

    title 'Раскопки'
    layout """
        IIIILIIII
        OOOOOOOOO
        IIIIIIIII
        IKKKKKKKI
        ${StringUtils.repeat('I', realDay)}U${StringUtils.repeat('I', 9 - realDay - 1)}
        IIJIIIHII
    """
    button MuseumGuis.background

    button 'L' icon {
        item NETHER_STAR
        text """§6Рынок
        
        Торги драгоценными камнями,
        ювелир, огранка, доход.
        
        Торги начнуться §b15 февраля 
        §bв 19:00 по МСК
        """
    } leftClick {
        performCommand("shop poly")
    }

    button 'U' icon {
        item CLAY_BALL
        nbt.other = 'arrow_up'
        text """§bСегодня доступна"""
    }

    def counter = 0
    GemType.values().collect().forEach { GemType type ->
        counter += 1
        def item = button 'K' icon {
            item CLAY_BALL
            nbt.museum = type.texture
            nbt.color = type.ordinal() + 1 == realDay ? 0xFFFFFFFF : 0x666666FF
            if (type.ordinal() + 1 == realDay)
                nbt.glow_color = 0x55555510
            text "§b§l${type.dayTag} §f${type.location} ${type.ordinal() + 1 == realDay ? "§aОткрыто" : "§cЗакрыто"}"
            text ""
            text "§7Можно найти §f${type.title}"
        }
        if (type.ordinal() + 1 == realDay) {
            item.leftClick {
                user.setState(App.app.crystal)
            }
        }
    }

    Managers.excavation.toSorted { a, b -> a.requiredLevel <=> b.requiredLevel }.each { excavation ->
        if (user.level >= excavation.requiredLevel) {
            button 'O' icon {
                apply Items.items['excavation-' + excavation.address]
                text """
                §e$excavation.title §6${moneyFormatter.format(excavation.price)}

                Опыт археолога: ${LevelSystem.acceptGiveExp(user, excavation.requiredLevel) ? "§aесть" : "§cнет"}
                Кол-во ударов: §e$excavation.hitCount

                §7Можно найти:
                """
                excavation.availableSkeletonPrototypes
                        .forEach(prototype -> text " §7- §b$prototype.title")
                excavation.relics.each {
                    relic -> text " §7- §a${relic.item.itemMeta.displayName}"
                }
            } leftClick {
                closeInventory()
                performCommand 'excavation ' + excavation.address
            }
        } else {
            button 'O' icon {
                item CLAY_BALL
                nbt.other = 'lock'
                text.clear()
                text """
                §8??? §f[§e$excavation.requiredLevel LVL§f]
                """
            }
        }
    }

    button 'J' icon {
        item SADDLE
        text '§6Магазин'
    } leftClick {
        performCommand("shop mono")
    }

    button 'H' icon {
        item CLAY_BALL
        nbt.other = 'guild_bank'
        text '§bМузей'
    } leftClick {
        performCommand("home")
    }
}