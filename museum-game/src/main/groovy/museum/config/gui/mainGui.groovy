package museum.config.gui

import clepto.bukkit.menu.Guis
import clepto.humanize.TimeFormatter
import museum.App
import museum.player.User
import museum.util.LevelSystem
import org.bukkit.Statistic
import org.bukkit.entity.Player

import java.text.DecimalFormat
import java.time.Duration

import static org.bukkit.Material.*

def formatter = TimeFormatter.builder() accuracy 500 build()
def moneyFormatter = new DecimalFormat('###,###,###,###,###,###.##$')

Guis.register 'main', { player ->

    User user = App.app.getUser((Player) player)

    title 'Главное меню'
    layout 'FH-SMT-JS'

    button MuseumGuis.background
    button 'F' icon {
        item PAPER
        text """
        §bПрофиль

        Уровень: $user.level
        Денег: ${moneyFormatter.format(user.money)}
        Опыт: $user.experience
        Опыта осталось: ${LevelSystem.formatExperience(user.experience)}
        Часов сыграно: ${user.player.getStatistic(Statistic.PLAY_ONE_TICK) / 720_000}
        Монет собрано: $user.pickedCoinsCount
        Кирка: $user.pickaxeType.name
        Раскопок: $user.excavationCount
        Фрагментов: ${user.skeletons.stream().mapToInt(s -> s.unlockedFragments.size()).sum()}
        """
    }

    button 'H' icon {
        item PAPER
    } rightClick {
        performCommand('gallery')
    }

    button 'S' icon {
        item GOLD_PICKAXE
        text """
        §bКирки

        Приобретите новую кирку,
        и разгодайте тайны песка...
        """
    } leftClick {
        performCommand('gui pickaxe')
    }

    button 'M' icon {
        item CLAY_BALL
        nbt([other: 'guild_bank'])
        def museum = user.lastMuseum
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

    button 'T' icon {
        item COMPASS
        text """
        §bЭкспедиции

        Отправтесь на раскопки
        и найдите следы прошлого.
        """
    } leftClick {
        performCommand('gui excavation')
    }

    button 'J' icon {
        item PAPER
    }

    button 'S' icon {
        item PAPER
    }

}


