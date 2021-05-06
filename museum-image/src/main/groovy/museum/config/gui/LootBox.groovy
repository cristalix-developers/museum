@groovy.transform.BaseScript(museum.MuseumScript)
package museum.config.gui

import clepto.bukkit.menu.Guis
import implario.ListUtils
import museum.App
import museum.client_conversation.AnimationUtil
import museum.donate.DonateType
import museum.fragment.Gem
import museum.fragment.GemType
import museum.fragment.Meteorite
import museum.player.User
import org.bukkit.entity.Player

import static org.bukkit.Material.BARRIER
import static org.bukkit.Material.CLAY_BALL

static void giveDrop(User owner) {
    def gem = new Gem(ListUtils.random(GemType.values()).name() + ":" + (0.6 + Math.random() / 100 * 40) + ":10000")
    gem.give(owner)
    def meteor = new Meteorite("meteor_" + ListUtils.random(Meteorite.Meteorites.values()).name())
    meteor.give(owner)

    AnimationUtil.topTitle owner, "Вы получили " + gem.type.title + " " + Math.round(gem.rarity * 100) + "% §fи " + meteor.meteorite.title + "!"
    AnimationUtil.throwIconMessage(owner, gem.item, gem.type.title + " " + Math.round(gem.rarity * 100) + "%", "и " + meteor.meteorite.title)
}

Guis.register 'loot', { Player player ->
    def user = App.app.getUser(player)

    title 'Черный рынок'
    layout "XXKXOXLXP"

    button MuseumGuis.background

    button 'K' icon {
        item CLAY_BALL
        nbt.museum = 'ruby'
        text """§aВы получите:

        §f㦶 Случайную драгоценность §6[60%-100%]§f:
        
            §b⭐⭐⭐ Рубин
            §b⭐⭐⭐ Сапфир
            §b⭐⭐⭐ Бриллиант
            §a⭐⭐ Изумруд
            §a⭐⭐ Шпиннель
            §7⭐ Аметист
            §7⭐ Танзанит
        """
    }

    button 'L' icon {
        item CLAY_BALL
        nbt.other = 'quest_day'
        text """§aВы получите:

        §f㩙 Случайный метеорит:
        
            §b⭐⭐⭐ Гоба 100дд
            §b⭐⭐⭐ Эйби 90дд
            §b⭐⭐⭐ Альфиане́лло 80дд
            §a⭐⭐ Нахла 50дд
            §a⭐⭐ Альенде 45дд
            §a⭐⭐ Гирин 40дд
            §a⭐⭐ Нортон Каунти 40дд
            §a⭐⭐ Куня-Ургенч 40дд
            §7⭐ Челябинский 30дд
            §7⭐ Бахмут 25дд
            §7⭐ Саттерз-Милл 20дд
            §7⭐ Андреевка 20дд
            §7⭐ Сихотэ-Алинский 20дд
            §7⭐ Башкувка 15дд
            §7⭐ Барботан 15дд
        """
    }

    def cost = 10000000

    button 'O' icon {
        item CLAY_BALL
        nbt.other = 'new_lvl_rare_close'
        text """§bСлучайная посылка

        §f㧩 Вы получите случайный 
        §fдрагоценный камень и метеорит!
        
        §f§lВНИМАНИЕ!
        
        §f[§bЛКМ§f] За игровую валюту §e10`000`000\$
        §f[§bПКМ§f] За кристалики ${DonateGui.modifyPrice(player.uniqueId, DonateType.ITEM_CASE.price)}
        """
    } leftClick {
        if (user.money > cost) {
            user.money = user.money - cost
            giveDrop(user)
            closeInventory()
        } else
            AnimationUtil.buyFailure(user)
    } rightClick {
        performCommand("proccessdonate ITEM_CASE")
    }

    button 'P' icon {
        item BARRIER
        text '§cНазад'
    } leftClick {
        performCommand("gui main")
    }
}