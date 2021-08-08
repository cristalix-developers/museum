@groovy.transform.BaseScript(museum.MuseumScript)
package museum.config.gui

import clepto.bukkit.menu.Guis
import implario.ListUtils
import museum.App
import museum.client_conversation.AnimationUtil
import museum.client_conversation.ModTransfer
import museum.donate.DonateType
import museum.fragment.Gem
import museum.fragment.GemType
import museum.fragment.Meteorite
import museum.player.User
import org.bukkit.ChatColor
import org.bukkit.Sound
import org.bukkit.SoundCategory
import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack
import org.bukkit.entity.Player

import static org.bukkit.Material.BARRIER
import static org.bukkit.Material.CLAY_BALL

static void giveDrop(User owner) {
    def gem = new Gem(ListUtils.random(GemType.values()).name() + ":" + (Math.random() * 1.1) + ":10000")
    gem.give(owner)
    def meteor = new Meteorite("meteor_" + ListUtils.random(Meteorite.Meteorites.values()).name())
    meteor.give(owner)

    new ModTransfer()
        .integer(2)
        .item(CraftItemStack.asNMSCopy(gem.item))
        .string(ChatColor.stripColor(gem.type.title + " " + Math.round(gem.rarity * 100F) + "%"))
        .string(getRare(gem.type.title))
        .item(CraftItemStack.asNMSCopy(meteor.item))
        .string(ChatColor.stripColor(meteor.item.getItemMeta().displayName))
        .string(getRare(meteor.item.getItemMeta().displayName))
        .send("lootbox", owner)
}

static String getRare(String string) {
    return string.contains("⭐⭐⭐") ? "LEGENDARY" : string.contains("⭐⭐") ? "EPIC" : "RARE"
}

registerCommand 'lootboxsound' handle {
    player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, SoundCategory.MASTER, 1, 2);
}

Guis.register 'loot', { Player player ->
    def user = App.app.getUser(player)

    title 'Лутбокс'
    layout "XXKXOXLXP"

    button MuseumGuis.background

    button 'K' icon {
        item CLAY_BALL
        nbt.museum = 'ruby'
        text """§aВы получите:

        §f㦶 Случайную драгоценность §6[0%-110%]§f:
        
            §6⭐⭐⭐ Рубин
            §6⭐⭐⭐ Сапфир
            §6⭐⭐⭐ Бриллиант
            §5⭐⭐ Изумруд
            §5⭐⭐ Шпиннель
            §b⭐ Аметист
            §b⭐ Танзанит
        """
    }

    button 'L' icon {
        item CLAY_BALL
        nbt.other = 'quest_day'
        text """§aВы получите:

        §f㩙 Случайный метеорит:
        
            §6⭐⭐⭐ Гоба 100дд
            §6⭐⭐⭐ Эйби 90дд
            §6⭐⭐⭐ Альфиане́лло 80дд
            §5⭐⭐ Нахла 50дд
            §5⭐⭐ Альенде 45дд
            §5⭐⭐ Гирин 40дд
            §5⭐⭐ Нортон Каунти 40дд
            §5⭐⭐ Куня-Ургенч 40дд
            §b⭐ Челябинский 30дд
            §b⭐ Бахмут 25дд
            §b⭐ Саттерз-Милл 20дд
            §b⭐ Андреевка 20дд
            §b⭐ Сихотэ-Алинский 20дд
            §b⭐ Башкувка 15дд
            §b⭐ Барботан 15дд
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
            user.giveMoney(-cost)
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