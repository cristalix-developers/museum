package museum.config.item

import static clepto.bukkit.item.Items.register
import static org.bukkit.Material.*

register 'menu', {
    item PAPER
    text """
        §f>> §6Меню §f<<
    
        Это меню, с помощью которого,
        настраивать музей, приглашать
        друзей, а так же смотреть
        подробную статистику.
    """
}

register 'visitor-menu', {
    item WOOD_DOOR
    text '§6Посмотреть музеи'
}

register 'place-menu', {
    item CLAY_BALL
    nbt.other = 'achievements_lock'
    text '§bГде я был?'
}