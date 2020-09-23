package museum.config.gui


import museum.museum.Museum
import org.bukkit.Material
import org.bukkit.entity.Player

import static clepto.bukkit.menu.Guis.register

import static museum.App.getApp
import static museum.config.gui.MuseumGuis.moneyFormatter

register 'visitor', { player ->
    def visitor = app.getUser((Player) player)

    title 'Посещение других музеев'

    layout """
        -OOOOOOO-
        -OOOOOOO-
        -OOOOOOO-
        -OOOOOOO-
        -OOOOOOO-
    """

    button MuseumGuis.background
    app.users.each {
        if (it == visitor)
            return
        def state = it.state
        if (!(state instanceof Museum))
            return
        def museum = ((Museum) state)
        if (visitor.state == museum || museum.getOwner() == visitor)
            return
        int fragments = it.skeletons.stream().mapToInt(s -> s.unlockedFragments.size()).sum().intValue()
        double price = fragments * 3.33f
        button 'O' icon {
            item Material.WOOD_DOOR
            text """Музей §b${museum.title.toLowerCase()}
            
            Цена визита: §6${moneyFormatter.format(price)}
            Фрагментов:  §b$fragments
            """
        } leftClick {
            performCommand "travel $museum.owner.name $price"
        }
    }
}