package museum.config.gui

import clepto.bukkit.item.Items
import clepto.bukkit.menu.Guis
import museum.App
import museum.prototype.Managers
import museum.util.CrystalUtil
import museum.util.LevelSystem
import org.apache.commons.lang.StringUtils
import org.bukkit.entity.Player

import java.text.DecimalFormat
import java.time.LocalDateTime
import java.time.ZoneId

import static org.bukkit.Material.*

def moneyFormatter = new DecimalFormat('###,###,###,###,###,###.##$')
def calendar = Calendar.getInstance()

Guis.register 'excavation', { player ->
    def user = App.app.getUser((Player) player)

    calendar.setTime(Date.from(LocalDateTime.now(ZoneId.of("Europe/Moscow"))
            .toLocalDate()
            .atStartOfDay()
            .atZone(ZoneId.systemDefault())
            .toInstant()
    ))

    def day = calendar.get(Calendar.DAY_OF_WEEK)
    def realDay = day == 1 ? 7 : day - 1

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

    ['ПН', 'ВТ', 'СР', 'ЧТ', 'ПТ', 'СБ', 'ВС'].forEach {String name ->
        button 'K' icon {
            item CLAY_BALL
            nbt.other = 'tochka'
            text "§b$name §fВременно недоступно"
        }
    }

    Managers.excavation.toSorted { a, b -> a.requiredLevel <=> b.requiredLevel }.each { excavation ->
        if (user.level >= excavation.requiredLevel) {
            button 'O' icon {
                apply Items.items['excavation-' + excavation.address]
                text """
                §e$excavation.title §6${moneyFormatter.format(excavation.price)} §7[ЛКМ] | §d${CrystalUtil.convertMoney2Crystal(excavation.price)} 㦶 §7[ПКМ]

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
                performCommand 'excavation ' + excavation.address + ' left'
            } rightClick {
                closeInventory()
                performCommand 'excavation ' + excavation.address + ' right'
            }
        } else {
            button 'O' icon {
                item CLAY_BALL
                nbt.other = 'tochka'
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