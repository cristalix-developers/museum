package museum.config.gui

import clepto.bukkit.item.Items
import clepto.bukkit.menu.Guis
import museum.App
import museum.museum.Museum
import museum.prototype.Managers
import museum.util.CrystalUtil
import museum.util.LevelSystem
import org.bukkit.entity.Player

import java.text.DecimalFormat

import static org.bukkit.Material.*

def moneyFormatter = new DecimalFormat('###,###,###,###,###,###.##$')

Guis.register 'excavation', { player ->
    def user = App.app.getUser((Player) player)

    title 'Раскопки'
    layout """
        IIIIIIIII
        OOOOOOOOO
        IIIIPIIII
        IIJIIIHII
        IIIIXIIII
    """
    button MuseumGuis.background

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
                    relic -> text " §7- §a${relic.relic.itemMeta.displayName}"
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

    if (user.state instanceof Museum) {
        button 'X' icon {
            item BARRIER
            text '§cНазад'
        } leftClick {
            performCommand("gui main")
        }
    }

    button 'J' icon {
        item SADDLE
        text '§6Магазин'
    } leftClick {
        performCommand("shop")
    }

    button 'H' icon {
        item CLAY_BALL
        nbt.other = 'guild_bank'
        text '§bМузей'
    } leftClick {
        performCommand("home")
    }

    button 'P' icon {
        item CLAY_BALL
        nbt.museum = 'crystal_pink'
        text """§bКристальная экспедиция
        
        &cБудет закрыта &fнавсегда &c15 
        &cфевраля &fв 00:00 по МСК.
        Все кристаллы автоматически 
        продадуться по курсу &d㦶 -> &8\$
        """
    } leftClick {
        performCommand("crystal")
    }
}