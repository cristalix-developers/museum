@groovy.transform.BaseScript(museum.MuseumScript)
package museum.config.command

import museum.App
import museum.misc.Relic
import museum.museum.subject.CollectorSubject
import museum.util.MessageUtil
import museum.util.SubjectLogoUtil
import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack

registerCommand 'sell' handle {
    def item = player.itemInHand
    if (item && item.itemMeta) {
        def user = App.app.getUser player
        def subject = SubjectLogoUtil.decodeItemStackToSubject(user, user.getInventory().getItemInHand())
        if (subject) {
            if (subject instanceof CollectorSubject)
                return null
            user.getInventory().setItemInHand(null)
            user.subjects.remove(subject)
            user.money = user.money + 1000
            return MessageUtil.find('stand-sell')
                    .set('price', 1000)
                    .getText()
        }
        def nmsItem = CraftItemStack.asNMSCopy item
        if (nmsItem.tag && nmsItem.tag.hasKeyOfType("relic", 8)) {
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