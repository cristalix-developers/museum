@groovy.transform.BaseScript(museum.MuseumScript)
package museum.config.gui

import museum.App
import museum.client_conversation.AnimationUtil
import museum.fragment.Fragment
import museum.fragment.Gem
import museum.util.MessageUtil
import org.bukkit.Material
import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack
import org.bukkit.entity.Player

import static clepto.bukkit.menu.Guis.register
import static org.bukkit.Material.*

register 'polishing', { player ->
    def user = App.app.getUser((Player) player)

    title 'Полировка'
    layout """
        OO--O--OO
        OO-OMO-OO
        OO-----OO
    """

    button MuseumGuis.background

    def itemInHand = user.player.itemInHand
    def tag = CraftItemStack.asNMSCopy(itemInHand).tag
    def price = 25000
    def chance = 0.40

    if (itemInHand == null || tag == null || !tag.hasKeyOfType('gem', 99)) {
        button 'M' icon {
            item CLAY_BALL
            text """
            §bЮвелир
            
            §7Вы можете поменять
            §7процент драгоценного
            §7камня (от 0 до 110%).
            
            §cШанс потерять камень ${(int) (chance * 100)}%
            §eСтоимость услуги ${MessageUtil.toMoneyFormat(price)}
            """
            nbt.other = 'anvil'
        }
        return
    }

    def gem = null
    for (Fragment currentRelic : user.relics) {
        if (currentRelic.uuid.toString() == tag.getString('relic-uuid')) {
            gem = currentRelic as Gem
        }
    }
    def gemItem = gem.item
    button 'M' icon {
        item gemItem.type
        text """
        $gemItem.itemMeta.displayName

        §7Вы можете поменять
        §7процент драгоценного
        §7камня (от §c0 §7до §b110%§7).
            
        §cШанс потерять камень ${(int) (chance * 100)}%
        §eСтоимость услуги ${MessageUtil.toMoneyFormat(price)}
        
        §aНажмите чтобы отполировать
        """
        nbt.museum = gem.type.texture
    } leftClick {
        if (user.money >= price) {
            player.inventory.removeItem itemInHand
            gem.remove user
            user.giveMoney(-price)
            closeInventory()
            if (Math.random() < chance)
                AnimationUtil.topTitle(user, '§cКамень был разрушен')
            else {
                new Gem(gem.type.name() + ":" + Math.random() * 0.95 + ":" + gem.price).give user
                AnimationUtil.topTitle user, '§aКамень был отполирован'
            }
        } else
            AnimationUtil.buyFailure user
    }
}