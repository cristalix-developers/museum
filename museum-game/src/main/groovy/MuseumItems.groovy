import clepto.humanize.TimeFormatter
import museum.museum.Museum

import java.time.Duration

import static clepto.bukkit.item.Items.register
import static org.bukkit.Material.CLAY_BALL
import static org.bukkit.Material.PAPER

def formatter = TimeFormatter.builder() accuracy 500 build()

register 'paper', {
    item PAPER
    text """
        §f>> §6§lМеню §f<<
    
        Это меню, с помощью которого,
        настраивать музей, приглашать
        друзей, а так же смотреть
        подробную статистику.
    """
}

register 'museum', {
    item CLAY_BALL
    nbt ([other: 'guild_bank'])
    def museum = context as Museum
    text """
        &bМузей
    
        Хозяин: &e$museum.owner.name
        Название: &e$museum.title
        Посещений: &e$museum.views
    
        Доход: &e$museum.income
        Витрин: &e${museum.subjects.size()}
    
        Создан &e${formatter.format(Duration.ofMillis(System.currentTimeMillis() - museum.creationDate.time))} назад
    """
}
