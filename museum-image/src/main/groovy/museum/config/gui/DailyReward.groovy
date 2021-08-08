@groovy.transform.BaseScript(museum.MuseumScript)
package museum.config.gui

import clepto.bukkit.menu.Gui
import museum.App
import museum.player.User
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player

import static clepto.bukkit.menu.Guis.register
import static clepto.bukkit.menu.Guis.staticButton
import static museum.App.*

register 'daily-reward', { Player player ->
    def userStatistic = app.getUser((Player) player)

    title 'Ежедневная награда'
    layout """
        ----I----
        ---I-I---
        -I-I-I-I-
        """

    int reward = 10
    int day = 0
    7.times {
        reward += 5
        day++

        button info('Награда за вход в игру', reward, day, userStatistic)
    }
}

static Gui.Button info(String title, int reward, int day, User user) {
    def button = staticButton 'I' icon {
        item Material.GOLD_NUGGET
        if (day == user.day)
            enchant(Enchantment.LUCK, 0)
        text """
        §e$title

        §7Ваша награда §b${reward}XP

        ${day == user.day ? '§eНажмите ЛКМ чтобы забрать награду.' : ''}
        """
        nbt.HideFlags = 63
    } leftClick {
        if (user.day == day) {
            user.giveMoney reward

            if (user.day >= 7)
                user.day = 1

            closeInventory()
        }
    }
    return button
}