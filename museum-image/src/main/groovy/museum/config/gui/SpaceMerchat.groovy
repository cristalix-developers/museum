@groovy.transform.BaseScript(museum.MuseumScript)
package museum.config.gui

import museum.App
import museum.client_conversation.AnimationUtil
import museum.cosmos.boer.Boer
import museum.cosmos.boer.BoerType
import museum.util.MessageUtil
import org.bukkit.Material
import org.bukkit.entity.Player

import static clepto.bukkit.menu.Guis.register

register 'boer', { player ->
    def user = App.app.getUser((Player) player)

    title 'Космический торговец'
    layout """
    ----O----
    """

    def money = 10000000
    def crystal = 10000
    button 'O' icon {
        item Material.CLAY_BALL
        text """
        &bКосмический бур
        
        &7Стоимость &a${MessageUtil.toMoneyFormat(money)} &7и &b$crystal &7Коспической руды

        &7Данный бур работает
        &7${BoerType.STANDARD.time / 3600 as int} час и приносит
        &b1 опыт &7и &b1 кристалл 
        &7каждые &l&f${BoerType.STANDARD.speed} &7секунд.
        """
        nbt.other = 'win2'
    } leftClick {
        if (user.getMoney() >= money && user.cosmoCrystal >= crystal) {
            user.giveMoney(-money)
            user.giveCosmoCrystal(-crystal, false)
            new Boer('boer_' + BoerType.STANDARD.name(), player.uniqueId).give(user)
        } else {
            AnimationUtil.buyFailure user
        }
    }
}