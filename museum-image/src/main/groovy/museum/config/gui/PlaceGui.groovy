@groovy.transform.BaseScript(museum.MuseumScript)
package museum.config.gui

import clepto.bukkit.menu.Guis
import museum.misc.PlacesMechanic
import org.bukkit.Material
import org.bukkit.entity.Player

import static museum.App.getApp
import static org.bukkit.Material.CLAY_BALL
import static org.bukkit.Material.COMPASS

on COMPASS use {
    Guis.open(player, 'place-gui', player)
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
    """

    button MuseumGuis.background
    button 'X' icon {
        item Material.DIAMOND
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
                item Material.PAPER
                text """§b${place.title}
            
                Вы получили §6${place.claimedExp} EXP
                """
            }
        }
    }
    21.times {
        button 'O' icon {
            item CLAY_BALL
            nbt.other = 'tochka'
            text '§8Место еще не найдено...'
        }
    }
}

