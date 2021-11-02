package museum.config.gui

import clepto.bukkit.item.ItemBuilder
import clepto.bukkit.menu.Gui
import clepto.bukkit.menu.Guis

import java.text.DecimalFormat

import static org.bukkit.Material.CLAY_BALL
import static org.bukkit.Material.STAINED_GLASS_PANE

class MuseumGuis {

    static DecimalFormat moneyFormatter = new DecimalFormat('###,###,###,###,###,###.##$')

    static Closure backgroundIcon = {
        ItemBuilder builder = (ItemBuilder) delegate
        builder.item STAINED_GLASS_PANE
        builder.data 8
        builder.text '&f'
    }

    static Gui.Button background = Guis.staticButton '-' fillAvailable() icon backgroundIcon

    static Gui.Button backToManipulator(uuid) {
        def button = Guis.staticButton('X').icon {
            item CLAY_BALL
            text '§cНазад'
            nbt.other = "cancel"
        } leftClick {
            Guis.open(delegate, 'manipulator', uuid)
        }
        return button
    }
}
