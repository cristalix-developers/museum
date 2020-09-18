package museum.config

import clepto.humanize.TimeFormatter
import museum.excavation.Excavation
import museum.museum.Museum
import museum.museum.subject.Subject
import museum.museum.subject.skeleton.Skeleton
import museum.player.User
import museum.util.LevelSystem
import org.bukkit.Statistic

import java.text.DecimalFormat
import java.time.Duration

import static clepto.bukkit.item.Items.register
import static org.bukkit.Material.*

def formatter = TimeFormatter.builder() accuracy 500 build()
def moneyFormatter = new DecimalFormat('###,###,###,###,###,###.##$')

register 'skeleton', {

    item BONE
    def skeleton = (Skeleton) context
    text "§f» Скелет §b$skeleton.prototype.title §f«"
    text '§eФрагменты: §f' + skeleton.unlockedFragments.size() + '§f/§e' + skeleton.prototype.fragments.size()
    text '§eОткуда: §f' + skeleton.prototype.rarity.period
    text '§eРазмер: §f' + skeleton.prototype.size + 'x' + skeleton.prototype.size

}

register 'unavailable', {
    item CLAY_BALL
    nbt.museum = 'point'
}

register 'collector-free', {
    item CLAY_BALL
    nbt.museum = 'car'
}

register 'collector-amateur', {
    item CLAY_BALL
    nbt.museum = 'car1'
}

register 'collector-professional', {
    item CLAY_BALL
    nbt.museum = 'car2'
}

register 'collector-prestige', {
    item CLAY_BALL
    nbt.museum = 'parovoz'
    text '&7???'
}

register 'gallery', {
    item STORAGE_MINECART
    text """
        &bПостройки

        Посетите галлерею построек
        и выбирите, что ходите приобрести
        для вашего музея!
    """
}

register 'back', {
    item SADDLE
    text """
        &bВернуться

        &7Нажмите ПКМ, чтобы вернуться
    """
}

register 'coin', {
    item DOUBLE_PLANT
}

register 'buy-floor', {
    item STONE_SLAB2
    text """
        §bКупить пол | §eСкоро...

        Приобретите полы, которые
        подходят вашему музею.
    """
}

register 'invite', {
    item BOOK_AND_QUILL
    text """
        §bПригласить друга

        Нажмите и введите
        никнейм приглашенного!
    """
}

register 'menu', {
    item PAPER
    text """
        §f>> §6§lМеню §f<<
    
        Это меню, с помощью которого,
        настраивать музей, приглашать
        друзей, а так же смотреть
        подробную статистику.
    """
}

register 'goto-excavations-item', {
    item COMPASS
    text """
        §bЭкспедиции

        Отправтесь на раскопки
        и найдите следы прошлого.
    """
}

register 'goto-pickaxes-item', {
    item GOLD_PICKAXE
    text """
        §bКирки

        Приобретите новую кирку,
        и разгодайте тайны песка...
    """
}

register 'goback', {
    item BARRIER
    text '§cВернуться'
}

register 'museum-change-title', {
    item SIGN
    text """
        §bПереименовать музей

        Если вам не нравтся
        название вашего музея
        вы можете его изменить.
    """
}

register 'emerald-item', {
    item EMERALD
    text """
        §aДрагоценный камень §6+200\$

        Иногда его можно найти
        на раскопках, стоит не много.
    """
}

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
    nbt([other: 'settings'])
    text """
        §bНастроить

        Настройки для постройки!
    """
}

register 'subject-destroy', {
    item CLAY_BALL
    nbt([other: 'guild_shop'])
    text """
        §bВ инвентарь!

        Переместит выбранную постройку,
        вам в инвентарь.
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

register 'go', {
    def excavation = context as Excavation
    item excavation.prototype.icon
    text """
        §b$excavation.prototype.title §6${moneyFormatter.format(excavation.prototype.price)}

        Минимальный уровень: $excavation.prototype.requiredLevel
        Кол-во ударов: $excavation.prototype.hitCount
    """
}

register 'profile', {
    item PAPER
    def user = context as User
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

register 'museum', {
    item CLAY_BALL
    nbt([other: 'guild_bank'])
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
