@groovy.transform.BaseScript(museum.MuseumScript)
package museum.config.command

import clepto.bukkit.menu.Guis
import museum.App
import museum.config.gui.MuseumGuis
import museum.misc.Relic
import museum.player.User
import museum.util.CrystalUtil
import museum.util.MessageUtil
import museum.util.SubjectLogoUtil
import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack
import org.bukkit.entity.Player

import static org.bukkit.Material.*

registerCommand 'sell' handle {
    def item = player.itemInHand
    if (item && item.itemMeta) {
        def user = App.app.getUser player
        def subject = SubjectLogoUtil.decodeItemStackToSubject(user, user.getInventory().getItemInHand())
        if (subject) {
            user.getInventory().setItemInHand(null)
            user.subjects.remove(subject)
            user.money = user.money + subject.prototype.price * 0.6
            return MessageUtil.find('stand-sell')
                    .set('price', subject.prototype.price * 0.6)
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
    } else {
        Guis.open(player, 'sell-menu', player)
    }
}

static String crystal2Money(User user, long crystal) {
    return "§e${MessageUtil.toMoneyFormat(CrystalUtil.convertCrystal2Money(user, crystal))}"
}

Guis.register 'sell-menu', {
    def user = App.app.getUser((Player) context)

    title 'Продажа'
    layout """
    ----X----
    --F-S-T--
    """
    if (user.crystal > 0) {
        layout """
        ----X----
        --F-S-T--
        ----M----
        """
        button 'M' icon {
            item MINECART
            text """§fПродать §d${user.crystal}㦶 §fза ${crystal2Money(user, user.crystal)}
        
            Продать все ваши кристаллы.
            """
        } leftClick {
            def temp = user.crystal
            user.crystal = 0
            user.money = user.money + CrystalUtil.convertCrystal2Money(user, temp)
            Guis.open(player, 'sell-menu', player)
            MessageUtil.find('sell-crystal')
                    .set('crystal', temp)
                    .set('money', crystal2Money(user, temp))
                    .send(user)
        }
    }
    button MuseumGuis.background
    button 'X' icon {
        item PAPER
        text """§fПерекупка

        В этом меню вы продаете
        §bкристаллы§f, чтобы продать
        реликвию или постройку (с вычетом §c§l40%§f), возьмите
        ее в руку и нажмите на меня снова.
        """
    }
    button 'F' icon {
        item CLAY_BALL
        nbt.museum = 'crystal_pink'
        text """§fПродать §d㦶 §fза  ${crystal2Money(user, 1)}
        
        §7У вас §d${user.getCrystal()}㦶
        """
    } leftClick {
        if (user.getCrystal() < 1)
            return
        user.crystal = user.crystal - 1
        user.money = user.money + CrystalUtil.convertCrystal2Money(user, 1)
        Guis.open(player, 'sell-menu', player)
        MessageUtil.find('sell-crystal')
                .set('crystal', 1)
                .set('money', crystal2Money(user, 1))
                .send(user)
    }

    def smallCrystal = 8
    def bigCrystal = 64

    button 'S' icon {
        item CLAY_BALL
        nbt.museum = 'crystal_pink'
        amount smallCrystal
        text """§fПродать §d${smallCrystal}㦶 §fза ${crystal2Money(user, smallCrystal + 1)}
        §7У вас §d${user.getCrystal()}㦶

        §eВыгода 11%
        """
    } leftClick {
        if (user.getCrystal() < smallCrystal)
            return
        user.crystal = user.crystal - smallCrystal
        user.money = user.money + CrystalUtil.convertCrystal2Money(user, smallCrystal + 1)
        Guis.open(player, 'sell-menu', player)
        MessageUtil.find('sell-crystal')
                .set('crystal', smallCrystal)
                .set('money', crystal2Money(user, smallCrystal + 1))
                .send(user)
    }
    button 'T' icon {
        item CLAY_BALL
        nbt.museum = 'crystal_pink'
        amount bigCrystal
        text """§fПродать §d${bigCrystal}㦶 §fза ${crystal2Money(user, bigCrystal + 12)}
        §7У вас §d${user.getCrystal()}㦶

        §eВыгода 20%
        """
    } leftClick {
        if (user.getCrystal() < bigCrystal)
            return
        user.crystal = user.crystal - bigCrystal
        user.money = user.money + CrystalUtil.convertCrystal2Money(user, bigCrystal + 12)
        Guis.open(player, 'sell-menu', player)
        MessageUtil.find('sell-crystal')
                .set('crystal', bigCrystal)
                .set('money', crystal2Money(user, bigCrystal + 12))
                .send(user)
    }
}