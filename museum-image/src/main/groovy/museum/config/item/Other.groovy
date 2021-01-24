package museum.config.item

import static clepto.bukkit.item.Items.register
import static org.bukkit.Material.BARRIER
import static org.bukkit.Material.CLAY_BALL
import static org.bukkit.Material.DOUBLE_PLANT
import static org.bukkit.Material.FISHING_ROD
import static org.bukkit.Material.SADDLE
import static org.bukkit.Material.STONE_SLAB2

register 'hook', {
    item FISHING_ROD
    text '&eКрюк'
    nbt.Unbreakable = 1
}

register 'unavailable', {
    item CLAY_BALL
    nbt.museum = 'point'
}

register 'coin', {
    item DOUBLE_PLANT
}

register 'back', {
    item SADDLE
    text """
        &bВернуться

        &7Нажмите ПКМ, чтобы вернуться
    """
}

register 'go-back-item', {
    item BARRIER
    text """
        §cПрекратить раскопки §7[§eПКМ-БЛОК§7]

        Вас экстренно вернут в ваш музей...
        Все, что вы открыли за это
        время - сохранится.
    """
}

register 'crystal', {
    item CLAY_BALL
    nbt.museum = 'crystal_pink'
    text """&bКристалл

    Очень ценится в международных 
    кругах, за него можно отправится
    в экспедиции без денег.
    """
}

register 'buy-floor', {
    item STONE_SLAB2
    text """
        §bКупить пол | §eСкоро...

        Приобретите полы, которые
        подходят вашему музею.
    """
}