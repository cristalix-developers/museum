package museum.config.gui

import implario.humanize.TimeFormatter
import museum.App
import museum.config.command.WagonConfig
import museum.museum.Museum
import museum.museum.map.SubjectType
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
        -H--M--L-
        O--STJ--P
        -S--F--K-
        ----X----
    """

    button MuseumGuis.background

    button 'X' icon {
        item CLAY_BALL
        text '§cНазад'
        nbt.other = "cancel"
    } leftClick {
        closeInventory()
    }

    button 'K' icon {
        item CLAY_BALL
        nbt.other = 'new_lvl_rare_close'
        text """§bЛутбокс

        §7㧩 Вы получите случайный 
        §7драгоценный камень [60%-100%],
        §7а так же случайный 
        §7метеорит доходом от 15\$ до 100\$.
        
        §l§aНажмите чтобы посмотреть!
        """
    } leftClick {
        open(player, 'loot', player)
    }

    button 'L' icon {
        item END_CRYSTAL
        text """
        §bПрефиксы

        §7Выберите префикс!
        §7Некоторые редкие префиксы
        §7дают бонусы.
        
        §l§aНажмите чтобы посмотреть!
        """
    } leftClick {
        performCommand('prefixes')
    }

    button 'P' icon {
        item GOLDEN_CARROT
        text """
        §bОсобое снаряжение
        §7
        §7Тут вы можете купить,
        §7интересные вещи...
        
        §l§aНажмите чтобы посмотреть!
        """
    } leftClick {
        closeInventory()
        open(player, 'donate', player)
    }

    def timePlayed = (user.timePlayed / 60_000).intValue()

    button 'F' icon {
        item PAPER
        text """
        §bПрофиль ${user.player.name}

        §7Уровень: §b$user.level
        §7Опыт: §b$user.experience 
        §7Денег: §a${moneyFormatter.format(user.money)}
        §7Время в игре: §f${(timePlayed / 60).toInteger()} ч. ${(timePlayed % 60)} мин.
        §7Монет собрано: §e$user.pickedCoinsCount
        §7Кирка: §f$user.pickaxeType.name
        §7Раскопок: §c$user.excavationCount
        §7Фрагментов: §c${user.skeletons.stream().mapToInt(s -> s.unlockedFragments.size()).sum()}
        """
    }

    button 'H' icon {
        item SIGN
        text """
        §bПереименовать музей

        §7Если вам не нравится
        §7название вашего музея
        §7вы можете его изменить.
        """
    } leftClick {
        performCommand('changetitle')
    }

    button 'S' icon {
        item GOLD_PICKAXE
        text """
        §bИнструменты
        
        §7Улучшайте ваше снаряжение.
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
    
        §7Хозяин: &e$museum.owner.name
        §7Название: &e$museum.title
        §7Посещений: &b$museum.views
        §7Доход: &a${moneyFormatter.format(museum.income)} 
        §7Витрин: &e${museum.getSubjects(SubjectType.SKELETON_CASE).size()}
   
        &aСоздан ${formatter.format(Duration.ofMillis(System.currentTimeMillis() - museum.creationDate.time))} назад
        """
    }

    button 'T' icon {
        item COMPASS
        text """
        §bЭкспедиции

        §7Отправляйтесь на раскопки
        §7и найдите следы прошлого.
        
        §l§aОтправиться в путь!
        """
    } leftClick {
        performCommand('gui excavation')
    }

    button 'J' icon {
        item BOOK_AND_QUILL
        text """
        §bПригласить друга

        §7Нажмите и введите
        §7никнейм приглашенного!
        """
    } leftClick {
        performCommand 'invite'
    }

    button 'S' icon {
        item STORAGE_MINECART
        text """
        &bЗаказать товар | &e$WagonConfig.COST\$

        §7Закажите фургон с продовольствием,
        §7он будет вас ждать слева от музея, 
        §7идите к желтому знаку за тем
        §7отнесите товар в лавку.
        """
    } leftClick {
        performCommand 'wagonbuy'
        closeInventory()
    }

    button 'O' icon {
        item ENDER_PEARL
        text """
        &bНочь &f/ &bДень

        §7Меняйте режим так, 
        §7как нравится глазам!
        """
    } leftClick {
        user.player.setPlayerTime(user.info.darkTheme ? 12000 : 21000, true)
        user.info.darkTheme = !user.info.darkTheme
        closeInventory()
    }
}
