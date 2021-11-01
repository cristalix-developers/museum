@groovy.transform.BaseScript(museum.MuseumScript)
package museum.config.listener

import clepto.bukkit.B
import museum.cosmos.Cosmos
import museum.cosmos.boer.Boer
import museum.excavation.Excavation
import museum.international.International
import museum.museum.Museum
import museum.util.TreasureUtil
import net.minecraft.server.v1_12_R1.EnumMoveType
import org.bukkit.Bukkit
import org.bukkit.Particle
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftArmorStand
import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractAtEntityEvent
import org.bukkit.event.player.PlayerInteractEvent
import ru.cristalix.core.formatting.Formatting

import static clepto.bukkit.item.Items.register
import static clepto.bukkit.menu.Guis.open
import static museum.App.app
import static museum.cosmos.Cosmos.JETPACK
import static museum.cosmos.Cosmos.ROCKET
import static org.bukkit.Material.*
import static org.bukkit.event.block.Action.LEFT_CLICK_BLOCK
import static org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK

on PAPER use {
    open player, 'main', null
}

on WOOD_DOOR use {
    player.performCommand 'gui visitor'
}

on SADDLE use {
    if (app.getUser(player).state instanceof International)
        return
    player.performCommand 'home'
}

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

def speed = 3
def MAX_BOER_COUNT = 6
on PlayerInteractEvent, {
    def user = app.getUser player
    def playerLocation = player.location
    playerLocation.yaw = 0
    playerLocation.pitch = 0
    def direction = player.eyeLocation.direction

    if (playerLocation.distanceSquared(ROCKET) < 45 && user.state instanceof Museum)
        user.setState(new Cosmos())

    if (user.state instanceof Cosmos && action != LEFT_CLICK_BLOCK) {
        def cosmos = user.state as Cosmos
        if (player.vehicle == null && player.itemInHand == JETPACK) {
            cosmos.useJetpack player
            return
        }

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
                    player.sendMessage Formatting.error('Вы не можете установить ещё один бур.')
                    return
                }
                currentRelic.view(user, clickedBlock.location.clone().add(0.0D, 1.0D, 0.0D))
            }
            return
        }

        def craftArmorStand = ((CraftArmorStand) stand).getHandle()
        B.postpone 3, {
            if (playerLocation.block.type != AIR) {
                craftArmorStand.killEntity()
                stand.remove()
                cosmos.stand = null
                def teleport = playerLocation.clone()
                do {
                    teleport.add(0,1,0)
                } while (teleport.block.type != AIR)
                player.teleport(teleport.add(0,2,0))
                return
            }
            craftArmorStand.move(
                    EnumMoveType.SELF,
                    direction.x * speed / 2,
                    direction.y * speed / 2,
                    direction.z * speed / 2
            )
        }
        craftArmorStand.move(
                EnumMoveType.SELF,
                direction.x * speed,
                direction.y * speed,
                direction.z * speed
        )
        LocationVerification.execute(user, stand)
        app.world.spawnParticle(Particle.EXPLOSION_LARGE, playerLocation, 5)
    }

    if (action != RIGHT_CLICK_BLOCK && action != Action.RIGHT_CLICK_AIR)
        return
    if (!(user.getState() instanceof Excavation))
        return
    TreasureUtil.sellAll(user)
}

on PlayerInteractAtEntityEvent, {
    def user = app.getUser(player)

    if (user.state instanceof Cosmos && clickedEntity.hasMetadata('boer')) {
        def owner = Bukkit.getPlayer(UUID.fromString(clickedEntity.getMetadata('owner')[0].asString()))
        def boer = UUID.fromString(clickedEntity.getMetadata('boer')[0].asString())
        def fragment = user.relics.get(boer) as Boer

        if (player.uniqueId != owner.uniqueId)
            player.sendMessage(Formatting.error('Этот бур не принадлежит вам.'))
        open(player, 'boer-upgrade', fragment)
    }
}
