package museum.config.item

import static clepto.bukkit.item.Items.register
import static org.bukkit.Material.CLAY_BALL

register 'collector-free', {
    item CLAY_BALL
    nbt.museum = 'car'
}

register 'prof-collector', {
    item CLAY_BALL
    nbt.museum = 'car1'
}

register 'pres-collector', {
    item CLAY_BALL
    nbt.museum = 'car2'
}

register 'punk-collector', {
    item CLAY_BALL
    nbt.museum = 'parovoz'
    text '&7???'
}