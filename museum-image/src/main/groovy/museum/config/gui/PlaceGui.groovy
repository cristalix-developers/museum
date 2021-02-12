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

def placeOnPage = 35

on PlayerInteractEvent, {
    if (item && item.type == CLAY_BALL) {
        def nmsItem = CraftItemStack.asNMSCopy(item)
        if (nmsItem.tag && nmsItem.tag.hasKeyOfType('other', 8))
            if (nmsItem.tag.getString('other') == 'achievements_lock')
                Guis.open(player, 'place-gui', player)
    }
}

Guis.register 'place-gui', {
    def page
    def user

    if (context instanceof Player) {
        page = 0
        user = app.getUser((Player) context)
    } else {
        def tuple = context as Tuple2
        page = tuple.v2 as Integer
        user = app.getUser((Player) tuple.v1)
    }

    def maxPages = user.claimedPlaces.size() / placeOnPage

    title 'Где я был? Страница ' + (1+page)

    layout """
        ----X----
        -OOOOOOO-
        -OOOOOOO-
        ${page > 0 ? 'B' : '-'}OOOOOOO${page < maxPages ? 'N' : '-'}
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
    button 'N' icon {
        item CLAY_BALL
        text '§bВперед'
        nbt.other = 'arrow_right'
    } leftClick {
        Guis.open(player, 'place-gui', new Tuple2(player, ++page))
    }
    button 'B' icon {
        item CLAY_BALL
        text '§bНазад'
        nbt.other = 'arrow_left'
    } leftClick {
        Guis.open(player, 'place-gui', new Tuple2(player, --page))
    }
    if (page * placeOnPage < user.claimedPlaces.size()) {
        user.claimedPlaces.collect().drop(page * placeOnPage).forEach {
            def place = PlacesMechanic.getPlaceByTitle it
            if (place) {
                button 'O' icon {
                    item CLAY_BALL
                    nbt.other = 'access'
                    text """§b${place.claimedMoney < 1 ? place.title : "Подарок"}
            
                Вы получили §6${place.claimedExp} EXP
                """
                }
            }
        }
    }
    button 'O' icon {
        item CLAY_BALL
        nbt.other = 'tochka'
        text '§8Место еще не найдено...'
    } fillAvailable()
}

