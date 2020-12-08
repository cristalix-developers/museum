package museum.config.gui


import clepto.bukkit.item.Items
import clepto.bukkit.menu.Guis
import museum.App
import museum.prototype.Managers
import museum.util.CrystalUtil
import org.bukkit.entity.Player

import java.text.DecimalFormat

import static org.bukkit.Material.BARRIER
import static org.bukkit.Material.CLAY_BALL

def moneyFormatter = new DecimalFormat('###,###,###,###,###,###.##$')

Guis.register 'excavation', { player ->
    def user = App.app.getUser((Player) player)

    title 'Раскопки'
    layout 'OOOOOOOOX'
    button MuseumGuis.background

    Managers.excavation.toSorted { a, b -> a.requiredLevel <=> b.requiredLevel }.each { excavation ->
        if (user.level >= excavation.requiredLevel) {
            button 'O' icon {
                apply Items.items['excavation-' + excavation.address]
                text """
                §e$excavation.title §6${moneyFormatter.format(excavation.price)} §7[ЛКМ] | §d${CrystalUtil.convertMoney2Cristal(excavation.price)} 㦶 §7[ПКМ]

                Минимальный уровень: §b$excavation.requiredLevel
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

    button 'X' icon {
        item BARRIER
        text '§cНазад'
    } leftClick {
        performCommand("gui main")
    }
}