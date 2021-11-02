@groovy.transform.BaseScript(museum.MuseumScript)
package museum.config.gui

import clepto.bukkit.menu.Gui
import museum.fragment.Gem
import museum.fragment.GemType
import museum.player.User
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player

import java.util.function.Consumer

import static clepto.bukkit.menu.Guis.register
import static clepto.bukkit.menu.Guis.staticButton
import static museum.App.getApp

register 'daily-reward', { Player player ->
    def userStatistic = app.getUser((Player) player)

    title 'Ежедневная награда'
    layout """
        ----I----
        ---I-I---
        -I-I-I-I-
        """

    button reward('Награда за первый вход в игру', 1, userStatistic, user -> user.giveMoney(10000))
    button reward('Награда за второй вход в игру', 2, userStatistic, user -> user.giveExperience(100))
    button reward('Награда за третий вход в игру', 3, userStatistic,
            user -> new Gem(GemType.RUBY.name() + ':' + 0.8 + ':10000'))
    button reward('Награда за четвёртый вход в игру', 4, userStatistic,
            user -> user.giveExperience(500))
    button reward('Награда за пятый вход в игру', 5, userStatistic,
            user -> user.giveMoney(50000))
    button reward('Награда за шестой вход в игру', 6, userStatistic,
            user -> LootBox.giveDrop(user))
    button reward('Награда за седьмой вход в игру', 7, userStatistic,
            user -> new Gem(GemType.BRILLIANT.name() + ':' + 1.0 + ':10000'))
}

static Gui.Button reward(String title, int day, User user, Consumer<User> give) {
    def button = staticButton 'I' icon {
        item Material.GOLD_NUGGET
        if (day == user.day)
            enchant(Enchantment.LUCK, 0)
        text """
        §e$title

        ${day == user.day ? '§eНажмите ЛКМ чтобы забрать награду.' : ''}
        """
        nbt.HideFlags = 63
    } leftClick {
        if (user.day == day) {
            give.accept user

            if (user.day >= 7)
                user.day = 1

            closeInventory()
        }
    }
    return button
}