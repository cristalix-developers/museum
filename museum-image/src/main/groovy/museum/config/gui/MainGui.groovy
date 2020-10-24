package museum.config.gui

import clepto.bukkit.menu.Guis
import clepto.humanize.TimeFormatter
import museum.App
import museum.config.command.WagonConfig
import museum.museum.Museum
import museum.museum.map.SubjectType
import museum.util.LevelSystem
import org.bukkit.entity.Player

import java.text.DecimalFormat
import java.time.Duration

import static org.bukkit.Material.*

def formatter = TimeFormatter.builder() accuracy 500 build()
def moneyFormatter = new DecimalFormat('###,###,###,###,###,###.##$')

Guis.register 'main', { player ->
    def user = App.app.getUser((Player) player)

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
        Часов сыграно: ${user.timePlayed / 3_600_000}
        Монет собрано: $user.pickedCoinsCount
        Кирка: $user.pickaxeType.name
        Раскопок: $user.excavationCount
        Фрагментов: ${user.skeletons.stream().mapToInt(s -> s.unlockedFragments.size()).sum()}
        """
    }

    button 'H' icon {
        item SIGN
        text """
        §bПереименовать музей

        Если вам не нравится
        название вашего музея
        вы можете его изменить.
        """
    } leftClick {
        performCommand('changetitle')
    }

    button 'S' icon {
        item GOLD_PICKAXE
        text """
        §bКирки

        Приобретите новую кирку,
        и разгадайте тайны песка...
        """
        nbt.HideFlags = 63
    } leftClick {
        performCommand('gui pickaxe')
    }

    button 'M' icon {
        item CLAY_BALL
        nbt.other = 'guild_bank'
        def museum = (Museum) user.state
        text """
        &bМузей
    
        Хозяин: &e$museum.owner.name
        Название: &e$museum.title
        Посещений: &b$museum.views
    
        Доход: &a$museum.income
        Витрин: &e${museum.getSubjects(SubjectType.SKELETON_CASE).size()}
    
        Создан &a${formatter.format(Duration.ofMillis(System.currentTimeMillis() - museum.creationDate.time))} назад
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
        item BOOK_AND_QUILL
        text """
        §bПригласить друга

        Нажмите и введите
        никнейм приглашенного!
        """
    } leftClick {
        performCommand 'invite'
    }

    button 'S' icon {
        item STORAGE_MINECART
        text """
        &bЗаказать товар | &e$WagonConfig.COST\$

        Закажите фургон с продовольствием,
        он будет вас ждать слева от музея, 
        идите к желтому знаку за тем
        &fотнесите товар в лавку.
        """
    } leftClick {
        performCommand 'wagonbuy'
        closeInventory()
    }

}
