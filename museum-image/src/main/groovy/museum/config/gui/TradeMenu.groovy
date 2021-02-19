@groovy.transform.BaseScript(museum.MuseumScript)
package museum.config.gui


import clepto.bukkit.menu.Guis
import museum.fragment.Fragment
import museum.fragment.Gem
import museum.international.Market
import museum.player.User
import museum.util.MessageUtil
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerInteractEntityEvent
import org.bukkit.inventory.EquipmentSlot
import ru.cristalix.core.formatting.Formatting

import static museum.App.getApp

on PlayerInteractEntityEvent, {
    def item = player.inventory.itemInHand

    if (!item || !item.hasItemMeta() || item.type != Material.CLAY_BALL || !player.sneaking)
        return null

    if (clickedEntity instanceof Player) {

        if (!clickedEntity.sneaking)
            return null

        if (hand == EquipmentSlot.HAND) {
            def user = app.getUser(player)
            def nmsItem = CraftItemStack.asNMSCopy(player.inventory.itemInHand)

            if (!nmsItem.tag || !nmsItem.tag.hasKeyOfType('relic', 8))
                return null

            if (user.state instanceof Market) {
                for (Fragment currentRelic : user.relics) {
                    if (currentRelic.uuid.toString() == nmsItem.tag.getString('relic-uuid')) {
                        def data = new Object[]{player, clickedEntity, currentRelic}
                        Guis.open(player, 'trade', data)
                        Guis.open(clickedEntity as Player, 'trade', data)
                        player.inventory.setItemInHand(null)
                        return null
                    }
                }
            }
        }
    }
    return null
}

Guis.register 'trade', { player ->
    def user = app.getUser((Player) player)

    def tuple
    User owner
    User victim
    Gem gem
    try {
        tuple = context as Object[]
        owner = app.getUser((Player) tuple[0])
        victim = app.getUser((Player) tuple[1])
        gem = tuple[2] as Gem
    } catch (Exception ignored) {
        return null
    }

    def privileges = owner.getInfo().privileges || victim.getInfo().privileges

    title owner.name + " x " + victim.name + (privileges ? " Комиссии §aнет" : " Комиссия §c20%")

    layout "----X----"

    button MuseumGuis.background

    def craftItem = CraftItemStack.asNMSCopy(gem.item)
    def cost = craftItem.tag.getInt('gem')

    def item = button 'X' icon {
        item Material.CLAY_BALL
        text """
        ${gem.item.itemMeta.displayName}

        Цена камня §a${MessageUtil.toMoneyFormat(cost)} §f/ §e${MessageUtil.toMoneyFormat(victim.money)}
        Прибыток §b~${Math.round(gem.realPrice / 100D)}~\$
        Редкость §b${Math.round(gem.rarity * 100)}%
        """
        nbt.museum = gem.type.texture
    }

    if (user == owner) {
        item.leftClick {
            victim.closeInventory()
            owner.closeInventory()
            owner.getInventory().addItem(gem.getItem())
        }
    } else {
        item.leftClick {
            victim.closeInventory()
            owner.closeInventory()

            if (victim.money <= cost) {
                owner.getInventory().addItem(gem.getItem())
                owner.sendMessage(Formatting.error("У оппонента недостаточно средств."))
                victim.sendMessage(Formatting.error("Вам не хватает денег."))
                return null
            }

            // Еще паранойя
            if (!Bukkit.getPlayer(owner.name) || !Bukkit.getPlayer(victim.name))
                return Formatting.error("Ваш опонент вышел из игры.")

            // Да-да опять, паранойя
            def clone = gem
            owner.relics.remove(gem)
            clone.give(victim)
            victim.setMoney(victim.money - cost)
            owner.setMoney(owner.money + cost * (privileges ? 1F : 0.8F))

            def message = Formatting.fine("Сделка совершена.")

            owner.sendMessage(message)
            victim.sendMessage(message)
        }
    }
}

registerCommand 'gemstat' handle {
    def user = app.getUser(player)
    def item = CraftItemStack.asNMSCopy(user.inventory.itemInHand)

    if (args.length < 1 || !item.tag || !item.tag.hasKeyOfType('gem', 99))
        return null

    def cost
    try {
        cost = Integer.parseInt(args[0])
    } catch (Exception ignored) {
        return Formatting.error("Возьмите камень в руку и напишите /gemstat [ЦЕНА]")
    }

    if (cost < 10 || cost > 300000000)
        return Formatting.error("Значение слишком маленькое или слишком большое!")

    for (Fragment currentRelic : user.relics) {
        if (currentRelic.uuid.toString() == item.tag.getString('relic-uuid')) {
            if (currentRelic instanceof Gem) {
                currentRelic.setPrice(cost)

                user.inventory.setItemInHand(currentRelic.item)

                return Formatting.fine("Цена камня изменена на " + MessageUtil.toMoneyFormat(cost))
            }
        }
    }
}