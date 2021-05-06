package museum.config.gui


import implario.humanize.TimeFormatter
import museum.App
import museum.config.command.WagonConfig
import museum.museum.Museum
import museum.museum.map.SubjectType
import museum.util.LevelSystem
import org.bukkit.entity.Player

import java.text.DecimalFormat
import java.time.Duration

import static clepto.bukkit.menu.Guis.open
import static clepto.bukkit.menu.Guis.register
import static org.bukkit.Material.*

def formatter = TimeFormatter.builder() accuracy 500 build()
def moneyFormatter = new DecimalFormat('###,###,###,###,###,###.##$')

register 'main', { player ->
    def user = App.app.getUser((Player) player)

    title 'Главное меню'
    layout """
        ----K----
        F--SMT--S
        -H--O--JP
    """

    button MuseumGuis.background

    button 'K' icon {
        item CLAY_BALL
        nbt.other = 'new_lvl_rare_close'
        text """§bСлучайная посылка

        §f㧩 Вы получите случайный 
        §fдрагоценный камень [60%-100%],
        §fа так же случайный 
        §fметеорит доходом от 15\$ до 100\$.
        
        §bНажмите чтобы посмотреть!
        """
    } leftClick {
        open(player, 'loot', player)
    }

    button 'P' icon {
        item GOLDEN_CARROT
        text """
        >> §bВнутриигровые покупки §f<<
        
        Тут вы можете купить,
        интересные вещи...
        """
    } leftClick {
        closeInventory()
        open(player, 'donate', player)
    }

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
        §bИнструменты
        
        Улучшайте ваше снаряжение.
        """
        nbt.HideFlags = 63
    } leftClick {
        performCommand('gui tools')
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
    
        Доход: &a${moneyFormatter.format(museum.income)} 
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

    button 'O' icon {
        item ENDER_PEARL
        text """
        &bНочь &f/ &bДень

        Меняйте режим так, как нравится глазам!
        """
    } leftClick {
        user.player.setPlayerTime(user.info.darkTheme ? 12000 : 21000, true)
        user.info.darkTheme = !user.info.darkTheme
        closeInventory()
    }
}
