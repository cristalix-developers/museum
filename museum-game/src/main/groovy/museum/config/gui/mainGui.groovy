package museum.config.gui

import clepto.bukkit.menu.Guis
import org.bukkit.Material

Guis.register 'main', {

    title 'Главное меню'
    layout '----S----'
    button MuseumGuis.background

    button 'S' icon {
        item Material.BONE
        text '&eOh, hello there.'
    } leftClick {
        sendMessage 'Привет, как дела?'
        closeInventory()
    }

}


