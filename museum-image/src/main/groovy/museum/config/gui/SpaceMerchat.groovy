@groovy.transform.BaseScript(museum.MuseumScript)
package museum.config.gui

import museum.App
import org.bukkit.Material
import org.bukkit.entity.Player

import static clepto.bukkit.menu.Guis.register

register 'space-merchant', { player ->
    def user = App.app.getUser((Player) player)

    title 'Космический торговец'
    layout """
        ---------
        ----O----
        ---------
    """

    button 'O' icon {
        item Material.CLAY_BALL
        text 'Бур'
    } leftClick {
        if (user.getMoney() >= 10000000 && user.getCosmoCrystal() >= 10000) {
            user.money = user.money - 10000000
            user.cosmoCrystal = user.cosmoCrystal - 10000
            // TODO Выдача бура
        }
    }
}