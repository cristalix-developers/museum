@groovy.transform.BaseScript(museum.MuseumScript)
package museum.config.command

import museum.App
import museum.util.MessageUtil
import museum.util.SubjectLogoUtil
import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack

registerCommand 'sell' handle {
    def item = player.itemInHand
    if (item && item.itemMeta) {
        def user = App.app.getUser player
        def subject = SubjectLogoUtil.decodeItemStackToSubject(user, user.getInventory().getItemInHand())
        if (subject && !subject.allocated) {
            user.getInventory().setItemInHand(null)
            user.subjects.remove(subject)
            user.giveMoney(subject.prototype.price * 0.6)
            return MessageUtil.find('stand-sell')
                    .set('price', MessageUtil.toMoneyFormat(subject.prototype.price * 0.6))
                    .getText()
        } else if (subject && subject.allocated) {
            user.getInventory().setItemInHand(null)
            return null
        }
        def nmsItem = CraftItemStack.asNMSCopy item
        if (nmsItem.tag && nmsItem.tag.hasKeyOfType("relic", 8)) {
            def currentRelic = user.relics.get(UUID.fromString(nmsItem.tag.getString('relic-uuid')))
            if (currentRelic == null)
                return
            def price = (currentRelic.address.contains("meteor") ? 20 : 1) * currentRelic.price
            player.itemInHand = null
            user.relics.remove currentRelic.uuid
            user.giveMoney(price)
            return MessageUtil.find('relic-sell')
                    .set('price', MessageUtil.toMoneyFormat(price))
                    .getText()
        }
    }
}

registerCommand 'sellall' handle {
    for (item in player.inventory) {
        if (item && item.itemMeta) {
            def user = App.app.getUser player
            def nmsItem = CraftItemStack.asNMSCopy item
            if (nmsItem.tag && nmsItem.tag.hasKeyOfType("relic", 8)) {
                def currentRelic = user.relics.get(UUID.fromString(nmsItem.tag.getString('relic-uuid')))
                if (currentRelic == null)
                    break
                def price = (currentRelic.address.contains("meteor") ? 20 : 1) * currentRelic.price
                player.inventory.remove(item)
                user.relics.remove currentRelic.uuid
                user.giveMoney(price)
                player.sendMessage(MessageUtil.find('relic-sell')
                        .set('price', MessageUtil.toMoneyFormat(price))
                        .getText()
                )
            }
        }
    }
}