@groovy.transform.BaseScript(museum.MuseumScript)
package museum.config.listener

import clepto.bukkit.B
import museum.cosmos.Cosmos
import museum.cosmos.boer.Boer
import museum.excavation.Excavation
import museum.international.International
import museum.multi_chat.ChatType
import museum.multi_chat.MultiChatUtil
import museum.museum.Museum
import museum.util.TreasureUtil
import org.bukkit.Bukkit
import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack
import org.bukkit.event.player.PlayerInteractAtEntityEvent
import org.bukkit.event.player.PlayerInteractEvent
import ru.cristalix.core.formatting.Formatting

import static clepto.bukkit.item.Items.register
import static museum.App.app
import static museum.cosmos.Cosmos.ROCKET
import static org.bukkit.Material.*
import static org.bukkit.event.block.Action.LEFT_CLICK_BLOCK
import static org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK

register 'sugar-treasure', {
    item SUGAR
    nbt.cost = 120
    text """
        §eСтранный белый песок §6+120\$

        Хм... Что же это, мы уже
        второй раз натыкаемся на него,
        пока ученые не раскрыли его тайну...
    """
}

register 'diamond-treasure', {
    item DIAMOND
    nbt.cost = 134
    text """
        §eДрагоценный минерал §6+134\$

        Иногда его можно найти
        на раскопках, стоит очень много!
    """
}

register 'ghost-treasure', {
    item GHAST_TEAR
    nbt.cost = 67
    text """
        §eСлеза Боба §6+67\$

        Ходят слухи, что она
        вообще не из этого мира...
    """
}

register 'arrow-treasure', {
    item ARROW
    nbt.cost = 46
    text """
        §eПервобытная стрела §6+46\$

        Откуда она тут?!
        Это еще предстоит узнать нашим
        ученым из музея...
    """
}

register 'emerald-treasure', {
    item EMERALD
    nbt.cost = 36
    text """
        §eДрагоценный камень §6+36\$

        Иногда его можно найти
        на раскопках, стоит не много.
    """
}

register 'flint-treasure', {
    item FLINT
    nbt.cost = 74
    text """
        §eПервобытное орудие §6+74\$

        Иногда его можно найти
        на раскопках, стоит не много.
    """
}

register 'sink-treasure', {
    item CLAY_BALL
    nbt.museum = 'sink'
    nbt.cost = 225
    text """
        §eДревняя раковина §6+225\$

        Древняя раковина, свидетельствует
        о мире до появления млекопитающих...
    """
}

def MAX_BOER_COUNT = 6
on PlayerInteractEvent, {
    def user = app.getUser player
    def playerLocation = player.location
    playerLocation.yaw = 0
    playerLocation.pitch = 0

    if (playerLocation.distanceSquared(ROCKET) < 45 && user.state instanceof Museum && user.getLevel() >= 300)
        B.postpone(1, () -> user.setState(new Cosmos()))

    if (user.state instanceof Cosmos && action != LEFT_CLICK_BLOCK) {
        def cosmos = user.state as Cosmos
        def stand = cosmos.stand

        if (stand == null) {
            if (action == RIGHT_CLICK_BLOCK) {
                def nmsItem = CraftItemStack.asNMSCopy(player.inventory.itemInHand)
                if (!nmsItem.tag || !nmsItem.tag.hasKeyOfType('boer-uuid', 8))
                    return
                def currentRelic = (Boer) user.relics.get(UUID.fromString(nmsItem.tag.getString('boer-uuid')))
                if (currentRelic == null)
                    return null
                if (user.relics.values().stream()
                        .filter { it instanceof Boer }
                        .filter { (it as Boer).isStanding() }.count() >= MAX_BOER_COUNT) {
                    MultiChatUtil.sendMessage(player, ChatType.SYSTEM, Formatting.error('Вы не можете установить ещё один бур.'))
                    return
                }
                currentRelic.view(user, clickedBlock.location.clone().add(0.0D, 1.0D, 0.0D))
            }
            return
        }

        if (!(user.getState() instanceof Excavation))
            return
        TreasureUtil.sellAll(user)
    }
}

on PlayerInteractAtEntityEvent, {
    def user = app.getUser(player)

    if (user.state instanceof Cosmos && clickedEntity.hasMetadata('boer')) {
        def owner = Bukkit.getPlayer(UUID.fromString(clickedEntity.getMetadata('owner')[0].asString()))
        def boer = UUID.fromString(clickedEntity.getMetadata('boer')[0].asString())

        if (player.uniqueId != owner.uniqueId) {
            MultiChatUtil.sendMessage(player, ChatType.SYSTEM, Formatting.error('Этот бур не принадлежит вам.'))
            return
        }

        player.performCommand("boermenu " + boer)
    }
}
