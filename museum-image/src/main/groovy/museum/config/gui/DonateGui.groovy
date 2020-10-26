@groovy.transform.BaseScript(museum.MuseumScript)
package museum.config.gui

import clepto.bukkit.menu.Guis
import org.bukkit.enchantments.Enchantment

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
    title 'Донат'
    layout """
        ---------
        -X-M-Z-Y-
        --N-J-D--
        ---------
    """

    button 'D' icon {
        item GOLDEN_APPLE
        text """
        §bЛокальный бустер денег §6§lx2 §f| §e99руб§f
        
        Бустер только для тебя на §b1 час§f,
        получаете в два раза больше денег!
        """
    } leftClick {
        performCommand("proccessdonate LOCAL_MONEY_BOOSTER")
    }

    button 'N' icon {
        item EXP_BOTTLE
        text """
        §bЛокальный бустер опыта §6§lx2 §f| §e99руб§f
        
        Бустер только для тебя на §b1 час§f,
        получаете в два раза больше опыта!
        """
    } leftClick {
        performCommand("proccessdonate LOCAL_EXP_BOOSTER")
    }

    button 'J' icon {
        item BEACON
        text """
        §bГлобальный бустер посетителей §6§lx3 §f| §e149руб§f
        
        Общий бустер на §b1 час§f,
        в ТРИ раза больше посетителей!
        """
    } leftClick {
        performCommand("proccessdonate GLOBAL_VILLAGER_BOOSTER")
    }

    button 'X' icon {
        item EXP_BOTTLE
        text """
        §bГлобальный бустер опыта §6§lx2 §f| §e149руб§f
        
        Общий бустер на §b1 час§f,
        все получат в два раза больше опыта!
        """
    } leftClick {
        performCommand("proccessdonate GLOBAL_EXP_BOOSTER")
    }

    button 'Y' icon {
        item GOLDEN_APPLE
        data 1
        text """
        §6Глобальный бустер денег §6§lx2 §f| §e199руб§f
        
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
        §6Стим-панк сборщик монет §f| §e249руб§f
        
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
        §bЛегендарная кирка §f| §e349руб§f
        
        Особая кирка, приносит 
        §b2 опыта за блок§f и
        вскапывает §bбольше всех 
        других! 
        """
    } leftClick {
        performCommand("proccessdonate LEGENDARY_PICKAXE")
    }
}