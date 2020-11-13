@groovy.transform.BaseScript(museum.MuseumScript)
package museum.config.command

import museum.App
import museum.misc.Relic
import museum.util.MessageUtil
import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack

registerCommand 'sellrelic' handle {
    def item = player.itemInHand
    if (item && item.itemMeta) {
        def nmsItem = CraftItemStack.asNMSCopy item
        if (nmsItem.tag && nmsItem.tag.hasKeyOfType("relic", 8)) {
            def user = App.app.getUser player
            for (Relic currentRelic : user.relics) {
                if (currentRelic.uuid.toString() == nmsItem.tag.getString('relic-uuid')) {
                    player.itemInHand = null
                    user.relics.remove currentRelic
                    user.depositMoneyWithBooster(currentRelic.price * 10)
                    return MessageUtil.find('relic-sell')
                            .set('price', currentRelic.price * 10)
                            .getText()
                }
            }
        }
    }
}