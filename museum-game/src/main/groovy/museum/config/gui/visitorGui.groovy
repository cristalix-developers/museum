package museum.config.gui


import clepto.bukkit.menu.Guis
import museum.App
import museum.museum.Museum
import org.bukkit.Material
import org.bukkit.entity.Player

import java.text.DecimalFormat

def moneyFormatter = new DecimalFormat('###,###,###,###,###,###.##$')

Guis.register 'visitor', { player ->
    def visitor = App.app.getUser((Player) player)

    title 'Посещение других музеев'

    layout """
    -OOOOOOO-
    -OOOOOOO-
    -OOOOOOO-
    -OOOOOOO-
    -OOOOOOO-
    """

    button MuseumGuis.background
    App.getApp().users.forEach(user -> {
        if (user == visitor)
            return
        def state = user.state
        if (state == null || !(state instanceof Museum))
            return
        def museum = ((Museum) state)
        if (visitor.state == museum || museum.getOwner() == visitor)
            return
        int fragments = user.skeletons.stream().mapToInt(s -> s.unlockedFragments.size()).sum().intValue()
        double price = fragments * 3.33f
        button 'O' icon {
            item Material.WOOD_DOOR
            text """Музей §b${museum.title.toLowerCase()}
            
            Цена визита: §6${moneyFormatter.format(price)}
            Фрагментов:  §b$fragments
            """
        } leftClick {
            performCommand("travel " + museum.owner.name + " " + price)
        }
    })
}