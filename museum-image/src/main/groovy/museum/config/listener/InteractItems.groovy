@groovy.transform.BaseScript(museum.MuseumScript)
package museum.config.listener

import clepto.bukkit.B
import clepto.bukkit.item.Items
import clepto.bukkit.menu.Guis
import museum.cosmos.Cosmos
import museum.excavation.Excavation
import museum.international.International
import museum.museum.Museum
import museum.util.TreasureUtil
import net.minecraft.server.v1_12_R1.EnumMoveType
import org.bukkit.Particle
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftArmorStand
import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.util.Vector

import static clepto.bukkit.item.Items.register
import static museum.App.app
import static museum.cosmos.Cosmos.JETPACK
import static museum.cosmos.Cosmos.ROCKET
import static org.bukkit.Material.*
import static org.bukkit.event.block.Action.*

on PAPER use {
    Guis.open player, 'main', null
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
def vector = new Vector(0, 3, 0)
def wall = CraftItemStack.asNMSCopy(new ItemStack(COBBLE_WALL))
def prismarine = CraftItemStack.asNMSCopy(new ItemStack(PRISMARINE))
def diamondBlock = CraftItemStack.asNMSCopy(new ItemStack(DIAMOND_BLOCK))
def anvil = CraftItemStack.asNMSCopy(new ItemStack(ANVIL))
def antenna = Items.render('antenna')
on PlayerInteractEvent, {
    def user = app.getUser player
    def playerLocation = player.location
    playerLocation.yaw = 0
    playerLocation.pitch = 0
    def direction = player.eyeLocation.direction

    /*new StandHelper(playerLocation)
        .isInvisible(true)
        .hasGravity(false)
        .isMarker(true)
        .slot(EnumItemSlot.HEAD, Items.render("relic-tooth"))
        .headPose(Math.PI / 7, Math.PI,0.0D)
        .isSmall(true)
        .build()

    new StandHelper(playerLocation.clone().add(0.0D,0.0D,0.2D))
            .isInvisible(true)
            .hasGravity(false)
            .isMarker(true)
            .slot(EnumItemSlot.HEAD, Items.render("relic-tooth"))
            .headPose(Math.PI / 7, -0.0D,0.0D)
            .isSmall(true)
            .build()

    new StandHelper(playerLocation.clone().add(0.0D, -0.5D, 0.1D))
            .isInvisible(true)
            .hasGravity(false)
            .isMarker(true)
            .slot(EnumItemSlot.HEAD, wall)
            .build()

    new StandHelper(playerLocation.clone().add(0.0D, -0.0D, 0.1D))
            .isInvisible(true)
            .hasGravity(false)
            .isMarker(true)
            .slot(EnumItemSlot.HEAD, prismarine)
            .build()

    new StandHelper(playerLocation.clone().add(-0.1D, 0.8D, 0.1D))
            .isInvisible(true)
            .hasGravity(false)
            .isMarker(true)
            .slot(EnumItemSlot.HEAD, diamondBlock)
            .isSmall(true)
            .build()

    new StandHelper(playerLocation.clone().add(0.0D, 1.3D, 0.1D))
            .isInvisible(true)
            .hasGravity(false)
            .isMarker(true)
            .slot(EnumItemSlot.HEAD, anvil)
            .isSmall(true)
            .build()

    new StandHelper(playerLocation.clone().add(0.0D, 0.5D, 0.1D))
            .isInvisible(true)
            .hasGravity(false)
            .isMarker(true)
            .slot(EnumItemSlot.HEAD, antenna)
            .build()*/

    if (playerLocation.distanceSquared(ROCKET) < 45 && user.state instanceof Museum)
        user.setState(new Cosmos())

    if (user.state instanceof Cosmos && action != LEFT_CLICK_BLOCK) {
        def cosmos = user.state as Cosmos
        if (player.vehicle == null && player.itemInHand == JETPACK) {
            cosmos.useJetpack player
            return
        }

        def stand = cosmos.stand

        if (stand == null)
            return

        def craftArmorStand = ((CraftArmorStand) stand).getHandle()
        B.postpone 3, {
            if (playerLocation.block.type != AIR) {
                craftArmorStand.killEntity()
                stand.remove()
                cosmos.stand = null
                player.teleport(playerLocation.clone().add(0, 1, 0))
                player.velocity = vector
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
        app.world.spawnParticle(Particle.EXPLOSION_LARGE, playerLocation, 5)
    }

    if (action != RIGHT_CLICK_BLOCK && action != RIGHT_CLICK_AIR)
        return
    if (!(user.getState() instanceof Excavation))
        return
    TreasureUtil.sellAll(user)
}
