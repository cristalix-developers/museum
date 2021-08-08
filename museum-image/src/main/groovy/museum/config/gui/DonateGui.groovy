@groovy.transform.BaseScript(museum.MuseumScript)
package museum.config.gui

import clepto.bukkit.menu.Guis
import museum.donate.DonateType
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player

import static org.bukkit.Material.*

public static String modifyPrice(UUID user, int price) {
    return "§b" + price + " кристаликов"
}

Guis.register 'donate', { player ->
    def user = (Player) player
    title '§bВнутриигровые покупки'
    layout """
        ----X----
        -X--J--Y-
        --P-M-Z--
    """

    button 'P' icon {
        item CLAY_BALL
        nbt.other = 'guild_members'
        text """
        §aКомиссия 0%

         ${modifyPrice(user.uniqueId, DonateType.PRIVILEGES.price)}

        Если вы §aпродаете или покупаете
        драгоценный камень, комиссия
        §aисчезнет§f, поэтому вы не теряете
        денег на переводах валюты.
        """
    } leftClick {
        performCommand("proccessdonate PRIVILEGES")
    }

    button 'X' icon {
        item END_CRYSTAL
        text """
        §aСлучайный префикс

         ${modifyPrice(user.uniqueId, DonateType.PREFIX_CASE.price)}
        
        §7Получите случайный префикс!
        
        Если такой префикс уже был?
        - §eВы получите §6§l50`000\$

        Каждое §dпятое §fоткрытие §dгарантирует
        §6редкий §fили §dэпичный §fпрефикс
        """
    } leftClick {
        performCommand("proccessdonate PREFIX_CASE")
    }

    button 'J' icon {
        item BEACON
        text """
        §aГлобальный бустер посетителей §6§lx3

        ${modifyPrice(user.uniqueId, DonateType.GLOBAL_VILLAGER_BOOSTER.price)}
        
        Общий бустер на §b1 час§f,
        в §lТРИ§f раза больше посетителей
        и §e§lмонет§f!
        """
    } leftClick {
        performCommand("proccessdonate GLOBAL_VILLAGER_BOOSTER")
    }

    button 'X' icon {
        item EXP_BOTTLE
        text """
        §bГлобальный бустер опыта §6§lx2

        ${modifyPrice(user.uniqueId, DonateType.GLOBAL_EXP_BOOSTER.price)}
        
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
        §eГлобальный бустер денег §6§lx2 

        ${modifyPrice(user.uniqueId, DonateType.GLOBAL_MONEY_BOOSTER.price)}
        
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

        ${modifyPrice(user.uniqueId, DonateType.STEAM_PUNK_COLLECTOR.price)}
        
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
        §b§lЛегендарная кирка

        ${modifyPrice(user.uniqueId, DonateType.LEGENDARY_PICKAXE.price)}
        
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