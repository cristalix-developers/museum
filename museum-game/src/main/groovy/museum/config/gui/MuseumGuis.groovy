package museum.config.gui

import clepto.bukkit.item.ItemBuilder
import clepto.bukkit.menu.Gui
import clepto.bukkit.menu.Guis

import static org.bukkit.Material.BARRIER
import static org.bukkit.Material.CLAY_BALL
import static org.bukkit.Material.STAINED_GLASS_PANE

class MuseumGuis {

    static Closure backgroundIcon = {
        ItemBuilder builder = (ItemBuilder) delegate
        builder.item STAINED_GLASS_PANE
        builder.data 8
        builder.text '&f'
    }

    static Gui.Button background = Guis.staticButton '-' fillAvailable() icon backgroundIcon

    static Gui.Button backToManipulator(subject) {
        def button = Guis.staticButton('X').icon {
            item BARRIER
            text '§cНазад'
        } leftClick {
            Guis.open(delegate, 'manipulator', subject)
        }
        return button
    }
}
