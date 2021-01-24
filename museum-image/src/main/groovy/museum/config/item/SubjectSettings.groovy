package museum.config.item

import museum.museum.subject.Subject

import static clepto.bukkit.item.Items.register
import static clepto.bukkit.item.Items.register
import static clepto.bukkit.item.Items.register
import static clepto.bukkit.item.Items.register
import static clepto.bukkit.item.Items.register
import static org.bukkit.Material.CLAY_BALL
import static org.bukkit.Material.CLAY_BALL
import static org.bukkit.Material.CONCRETE
import static org.bukkit.Material.MILK_BUCKET
import static org.bukkit.Material.PAPER

register 'subject-change-color', {
    item MILK_BUCKET
    text """
        §bИзменить цвет

        Кастомизируйте цвета построек,
        так, как вам нравится!
    """
}

register 'subject-special', {
    item CLAY_BALL
    nbt.other = 'settings'
    text """
        §bНастроить

        Настройки для постройки!
    """
}

register 'subject-destroy', {
    item CLAY_BALL
    nbt.other = 'guild_shop'
    text """
        §bВ инвентарь!

        Переместит выбранную постройку,
        вам в инвентарь.
    """
}

register 'subject-color', {
    item CONCRETE
    def colorName = context as String
    text "&bВыбрать $colorName&b цвет!"
}

register 'subject-info', {
    item PAPER
    def subject = context as Subject
    text """
        §b$subject.prototype.title

        Стоимость: $subject.prototype.price
    """
}