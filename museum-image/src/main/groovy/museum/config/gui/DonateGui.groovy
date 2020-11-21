@groovy.transform.BaseScript(museum.MuseumScript)
package museum.config.gui

import clepto.bukkit.menu.Guis
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player

import static org.bukkit.Material.*

static String modifyPrice(UUID user, int price) {
    return "§b" + price + " кристаликов"
}

Guis.register 'donate', { player ->
    def user = (Player) context
    title '§bВнутриигровые покупки'
    layout """
        -X-M-Z-Y-
        ----J----
    """
/*
    button 'D' icon {
        item GOLDEN_APPLE
        text """
        §bЛокальный бустер денег §6§lx2
        §b99 кристальков§f
        
        Бустер только для тебя на §b1 час§f,
        получаете в два раза больше денег!
        """
    } leftClick {
        performCommand("proccessdonate LOCAL_MONEY_BOOSTER")
    }

    button 'N' icon {
        item EXP_BOTTLE
        text """
        §bЛокальный бустер опыта §6§lx2
        §b99 кристаликов§f
        
        Бустер только для тебя на §b1 час§f,
        получаете в два раза больше опыта!
        """
    } leftClick {
        performCommand("proccessdonate LOCAL_EXP_BOOSTER")
    }*/

    button 'J' icon {
        item BEACON
        text """
        §bГлобальный бустер посетителей §6§lx3
        ${modifyPrice(user.uniqueId, 149)}
        
        Общий бустер на §b1 час§f,
        в ТРИ раза больше посетителей!
        """
    } leftClick {
        performCommand("proccessdonate GLOBAL_VILLAGER_BOOSTER")
    }

    button 'X' icon {
        item EXP_BOTTLE
        text """
        §bГлобальный бустер опыта §6§lx2
        ${modifyPrice(user.uniqueId, 149)}
        
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
        §6Глобальный бустер денег §6§lx2 
        ${modifyPrice(user.uniqueId, 199)}
        
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
        ${modifyPrice(user.uniqueId, 249)}
        
        §bБыстрее всех§f! Собирает самые
        дальние монеты -§b лучший выбор
        среди коллекторов.

        §7Не остается после вайпа
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
        ${modifyPrice(user.uniqueId, 349)}
        
        Особая кирка, приносит 
        §b2 опыта за блок§f и
        вскапывает §bбольше всех 
        других! 

        §7Не остается после вайпа
        """
    } leftClick {
        performCommand("proccessdonate LEGENDARY_PICKAXE")
    }
}