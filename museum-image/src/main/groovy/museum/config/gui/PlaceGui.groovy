@groovy.transform.BaseScript(museum.MuseumScript)
package museum.config.gui

import clepto.bukkit.menu.Guis
import museum.misc.PlacesMechanic
import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerInteractEvent

import static museum.App.getApp
import static org.bukkit.Material.CLAY_BALL
import static org.bukkit.Material.DIAMOND

on PlayerInteractEvent, {
    if (item && item.type == CLAY_BALL) {
        def nmsItem = CraftItemStack.asNMSCopy(item)
        if (nmsItem.tag && nmsItem.tag.hasKeyOfType('other', 8))
            if (nmsItem.tag.getString('other') == 'achievements_lock')
                player.performCommand 'place'
    }
}

registerCommand 'place' handle {
    Guis.open(player, 'place-gui', player)
    return
}

Guis.register 'place-gui', {
    def user = app.getUser((Player) context)

    title 'Где я был(а)?'

    layout """
        ----X----
        -OOOOOOO-
        -OOOOOOO-
        -OOOOOOO-
        -OOOOOOO-
        -OOOOOOO-
    """

    button MuseumGuis.background
    button 'X' icon {
        item DIAMOND
        text """§bМеста!
            
                Гуляйте по картам и
                находите интересные места,
                за которые вы получаете §bопыт
                для повышения уровня! 
                """
    }
    user.claimedPlaces.forEach {
        def place = PlacesMechanic.getPlaceByTitle it
        if (place) {
            button 'O' icon {
                item CLAY_BALL
                nbt.other = 'access'
                text """§b${place.title}
            
                Вы получили §6${place.claimedExp} EXP
                """
            }
        }
    }
    button 'O' icon {
        item CLAY_BALL
        nbt.other = 'tochka'
        text '§8Место еще не найдено...'
    } fillAvailable()
}

