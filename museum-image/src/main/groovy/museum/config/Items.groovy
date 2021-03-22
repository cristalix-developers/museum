@groovy.transform.BaseScript(museum.MuseumScript)
package museum.config

import museum.museum.subject.Subject
import museum.museum.subject.skeleton.Skeleton

import static clepto.bukkit.item.Items.register
import static org.bukkit.Material.*

register 'hook', {
    item FISHING_ROD
    text '&eКрюк'
    nbt.Unbreakable = 1
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

register 'relic-totem', {
    item CLAY_BALL
    nbt.price = 1400
    nbt.relic = 'totem'
    nbt.museum = 'totem'
    nbt.glow_color = 0x50008000
    text """⭐⭐⭐ §eТотем Майя

    &eДоход оценивается в 14\$
    
    Можно продать перекупщику 

    &7Поставьте реликвию на витрину для
    &7реликвий, которую вы можете купить
    &7в магазине построек.
    """
}

register 'relic-shap', {
    item CLAY_BALL
    nbt.price = 800
    nbt.relic = 'shap'
    nbt.museum = 'shap'
    nbt.glow_color = 0x50808080
    text """⭐ §eМаска война Майя

    &eДоход оценивается в 8\$
    
    Можно продать перекупщику 

    &7Поставьте реликвию на витрину для
    &7реликвий, которую вы можете купить
    &7в магазине построек.
    """
}

register 'relic-yantar', {
    item CLAY_BALL
    nbt.price = 2000
    nbt.relic = 'yantar'
    nbt.museum = 'yantar'
    nbt.glow_color = 0x50000080
    text """⭐⭐⭐ §eЯнтарь

    &eДоход оценивается в 20\$
    
    Можно продать перекупщику 

    &7Поставьте реликвию на витрину для
    &7реликвий, которую вы можете купить
    &7в магазине построек.
    """
}


register 'relic-tooth', {
    item CLAY_BALL
    nbt.price = 900
    nbt.relic = 'tooth'
    nbt.museum = 'tooth'
    nbt.glow_color = 0x50808080
    text """⭐ §eЗуб мегалодона

    &eДоход оценивается в 9\$
        
    Можно продать перекупщику 

    &7Поставьте реликвию на витрину для
    &7реликвий, которую вы можете купить
    &7в магазине построек.
    """
}

register 'relic-anubis', {
    item CLAY_BALL
    nbt.price = 1200
    nbt.relic = 'anubis'
    nbt.museum = 'anubisstick'
    nbt.glow_color = 0x50008000
    text """⭐⭐ §eОрудие Анубиса

    &eДоход оценивается в 12\$
        
    Можно продать перекупщику 

    &7Поставьте реликвию на витрину для
    &7реликвий, которую вы можете купить
    &7в магазине построек.
    """
}

register 'relic-pot', {
    item CLAY_BALL
    nbt.price = 700
    nbt.relic = 'pot'
    nbt.museum = 'pot'
    nbt.glow_color = 0x50808080
    text """⭐ §eДревний горшок

    &eДоход оценивается в 7\$
        
    Можно продать перекупщику 

    &7Поставьте реликвию на витрину для
    &7реликвий, которую вы можете купить
    &7в магазине построек.
    """
}

register 'relic-shield', {
    item CLAY_BALL
    nbt.price = 1000
    nbt.relic = 'shield'
    nbt.museum = 'shieldofruin'
    nbt.glow_color = 0x50008000
    text """⭐⭐ §eЩит война Древнего Египта

    &eДоход оценивается в 10\$
        
    Можно продать перекупщику 

    &7Поставьте реликвию на витрину для
    &7реликвий, которую вы можете купить
    &7в магазине построек.
    """
}

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

register 'goback', {
    item BARRIER
    text '§cВернуться'
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
