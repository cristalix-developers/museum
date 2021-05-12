@groovy.transform.BaseScript(museum.MuseumScript)
package museum.config.command

import museum.App
import museum.fragment.Fragment
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
            user.money = user.money + subject.prototype.price * 0.6
            return MessageUtil.find('stand-sell')
                    .set('price', MessageUtil.toMoneyFormat(subject.prototype.price * 0.6))
                    .getText()
        } else if (subject && subject.allocated) {
            user.getInventory().setItemInHand(null)
            return null
        }
        def nmsItem = CraftItemStack.asNMSCopy item
        if (nmsItem.tag && nmsItem.tag.hasKeyOfType("relic", 8)) {
            for (Fragment currentRelic : user.relics) {
                if (currentRelic.uuid.toString() == nmsItem.tag.getString('relic-uuid')) {
                    def price = 0

                    if (currentRelic.address.contains("meteor"))
                        price += 20 * currentRelic.price
                    else
                        price += currentRelic.price
                    player.itemInHand = null
                    user.relics.remove currentRelic
                    user.money += price
                    return MessageUtil.find('relic-sell')
                            .set('price', MessageUtil.toMoneyFormat(price))
                            .getText()
                }
            }
        }
    }
}