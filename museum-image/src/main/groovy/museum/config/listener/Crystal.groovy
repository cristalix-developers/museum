@groovy.transform.BaseScript(museum.MuseumScript)
package museum.config.listener

import clepto.bukkit.B
import clepto.bukkit.item.Items
import museum.App
import museum.client_conversation.AnimationUtil
import museum.fragment.Gem
import museum.fragment.GemType
import museum.international.International
import museum.player.User
import museum.util.MessageUtil
import net.minecraft.server.v1_12_R1.EntityArmorStand
import net.minecraft.server.v1_12_R1.EnumItemSlot
import net.minecraft.server.v1_12_R1.MinecraftServer
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.craftbukkit.v1_12_R1.CraftWorld
import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.event.block.Action
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.player.PlayerFishEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerToggleSneakEvent
import org.bukkit.inventory.ItemStack
import ru.cristalix.core.formatting.Formatting

import static museum.boosters.BoosterType.EXP

def redstone = new ItemStack(Material.REDSTONE_BLOCK)

Items.register 'tnt', {
    item Material.TNT
    text "§c§lДинамит §fЛКМ"
    amount 3
}

def tntNms = Items.render('tnt')
def tnt = CraftItemStack.asCraftMirror(tntNms)

registerCommand 'hide' handle {
    def user = App.app.getUser(player)
    if (user.state instanceof International) {
        user.hideFromAll()
        return Formatting.fine("Вы скрыли игроков и спрятались от социума.")
    }
}

registerCommand 'show' handle {
    def user = App.app.getUser(player)
    if (user.state instanceof International) {
        user.showToAllState()
        return Formatting.fine("Вы снова в строю, все вас видят.")
    }
}

def actual = GemType.getActualGem()

Items.register 'ore', {
    item Material.CLAY_BALL
    nbt.museum = actual.oreTexture
    nbt.ore = actual.name()
    text "§bРуда "
    text "После выхода из шахты исчезает"
    text "Держите в руках и нажмите SHIFT"
    text "у водопромывочной станции."
}

def item = CraftItemStack.asCraftMirror(Items.render('ore'))

on PlayerFishEvent, {
    if (state == PlayerFishEvent.State.CAUGHT_FISH) {
        expToDrop = 0
        def user = App.app.getUser(player)
        def exp = (int) (Math.ceil(Math.random() * 3 + 3) * App.app.playerDataManager.calcGlobalMultiplier(EXP))
        user.giveExperience(exp)
        MessageUtil.find('fishing').set('exp', exp).send(user)
        return
    }
    def hookLocation = getHook().location.clone()
    if (hookLocation.subtract(0, 0.3, 0).block.type == Material.AIR)
        return
    if (getHook().location.distanceSquared(player.location) > 300)
        return
    player.setVelocity(hookLocation.subtract(player.location).toVector() * 0.3)
}

on EntityDamageByEntityEvent, {
    if (damager instanceof Player && entity instanceof ArmorStand) {
        def user = App.app.getUser(damager as Player)
        if (user.state instanceof International && entity.helmet.type == Material.STONE) {
            user.inventory.addItem(item)
        }
    }
}

on PlayerInteractEvent, {
    def user = App.app.getUser(player)
    if (!(user.state instanceof International))
        return null
    if (action == Action.PHYSICAL && player.location.getBlock().type == Material.STONE_PLATE) {
        tnt.amount = 3
        player.inventory.addItem(tnt)
    } else if (action == Action.LEFT_CLICK_AIR) {
        if (player.itemInHand.type == Material.TNT) {
            player.itemInHand.setAmount(player.itemInHand.amount - 1)

            def myWorld = player.world
            def world = ((CraftWorld) myWorld).handle
            def location = player.location
            def vector = player.eyeLocation.getDirection()
            def stand = new EntityArmorStand(world)
            stand.setPosition(location.getX(), location.getY(), location.getZ())
            stand.motX = vector.x
            stand.motY = vector.y
            stand.motZ = vector.z
            stand.setSlot(EnumItemSlot.HEAD, tntNms)
            stand.setSmall(true)
            stand.setInvisible(true)

            world.addEntity(stand)

            B.postpone(20, () -> {
                MinecraftServer.SERVER.postToNextTick(() -> {
                    stand.killEntity()
                    world.removeEntity(stand)
                })

                def newLocation = new Location(myWorld, stand.x, stand.y, stand.z)
                myWorld.spawnParticle(Particle.EXPLOSION_LARGE, newLocation, 1)

                def entities = newLocation.clone().subtract(0, 1.5, 0).getNearbyEntities(5, 5, 5)
                ArmorStand currentEntity

                for (Entity entity : entities) {
                    if (!(entity instanceof ArmorStand))
                        return null
                    if (entity.equipment.helmet.type == Material.REDSTONE_BLOCK) {
                        AnimationUtil.cursorHighlight(user, '§c§lБУМ!')

                        entity.helmet = null

                        currentEntity = entity

                        B.postpone(50, () -> {
                            currentEntity.helmet = redstone
                        })

                        if (Math.random() < 0.9) {
                            user.giveExperience(1)
                            AnimationUtil.cursorHighlight(user, '§b§l+1 §fопыт')
                        }
                        if (Math.random() < 0.01) {
                            def gem = new Gem(actual.name() + ":" + Math.random() / 1.9 + ":10000")
                            gem.give(user)
                            AnimationUtil.throwIconMessage(user, gem.item, gem.type.title, "Находка!")
                            App.app.users.forEach { User currentUser ->
                                if (currentUser == user)
                                    return null
                                AnimationUtil.cursorHighlight(currentUser, "§b" + user.name + " §f" + actual.title + " §b" + Math.round(gem.rarity * 100) + "%")
                            }
                        }
                        break
                    }
                }
            })
        }
    }
}

on PlayerToggleSneakEvent, {
    if (player.isSneaking()) {
        if (!player.itemInHand || player.itemInHand.type != Material.CLAY_BALL)
            return null
        def user = App.app.getUser(player)
        if (!(user.state instanceof International))
            return null
        def entities = player.location.getNearbyEntities(3, 2, 3)
        for (Entity entity : entities) {
            if (!(entity instanceof ArmorStand))
                return null
            if (entity.equipment.itemInHand.type == Material.CLAY_BALL) {
                if (player.itemInHand.type == Material.CLAY_BALL) {
                    def craftItem = CraftItemStack.asNMSCopy(player.itemInHand)
                    if (craftItem.tag && craftItem.tag.hasKeyOfType('ore', 8)) {
                        player.itemInHand.setAmount(player.itemInHand.amount - 1)
                        if (Math.random() < 0.3) {
                            user.giveExperience(1)
                            AnimationUtil.cursorHighlight(user, '§b§l+1 §fопыт')
                        }
                        if (Math.random() < 0.001) {
                            def gem = new Gem(actual.name() + ":" + Math.random() / 2 + ":10000")
                            gem.give(user)
                            AnimationUtil.throwIconMessage(user, gem.item, gem.type.title, "Находка!")
                            App.app.users.forEach { User currentUser ->
                                if (currentUser == user)
                                    return null
                                AnimationUtil.cursorHighlight(currentUser, "§b" + user.name + " §f" + actual.title + " §b" + Math.round(gem.rarity * 100) + "%")
                            }
                        }
                        break
                    }
                }
            }
        }
    }
}