@groovy.transform.BaseScript(museum.MuseumScript)
package museum.config.gui

import clepto.bukkit.menu.Guis
import museum.App
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player

import static clepto.bukkit.item.Items.register
import static org.bukkit.Material.*

register 'donate-menu', {
    item GOLDEN_CARROT
    text """
        >> §6Донат §f<<
        
        Тут вы можете купить,
        интересные вещи...
    """
}

on GOLDEN_CARROT use {
    Guis.open player, 'donate', player
}

Guis.register 'donate', { player ->
    def user = App.app.getUser((Player) player)

    title 'Раскопки'
    layout '-M-Z-X-Y-'

    button 'X' icon {
        item EXP_BOTTLE
        text """
        §bГлобальный бустер опыта §6§lx2
        
        Общий бустер на §b1 час§f,
        все получат в два раза больше опыта!
        """
    } leftClick {
        performCommand("proccessdonate GLOBAL_EXP_BOOSTER")
    }

    button 'Y' icon {
        item GOLD_BLOCK
        text """
        §6Глобальный бустер денег §6§lx2
        
        Общий бустер на §b1 час§f,
        все получат в два раза больше денег!
        """
    } leftClick {
        performCommand("proccessdonate GLOBAL_MONEY_BOOSTER")
    }

    button 'Z' icon {
        item CLAY_BALL
        nbt.museum = 'parovoz'
        text """
        §6Стим-панк сборщик монет
        
        §bБыстрее всех§f! Собирает самые
        дальние монеты -§b лучший выбор
        среди коллекторов.
        """
    } leftClick {
        performCommand("proccessdonate STEAM_PUNK_COLLECTOR")
    }

    button 'M' icon {
        item DIAMOND_PICKAXE
        enchant(Enchantment.DIG_SPEED, 1)
        nbt.HideFlags = 63
        nbt.prison = '23feb'
        text """
        §bЛегендарная кирка
        
        Особая кирка, приносит 
        §b3 опыта за блок§f и
        вскапывает §bбольше всех 
        других! 
        """
    } leftClick {
        performCommand("proccessdonate LEGENDARY_PICKAXE")
    }
}