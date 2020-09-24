package museum.config.gui

import clepto.bukkit.item.Items
import clepto.bukkit.menu.Guis
import museum.App
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player

import static clepto.bukkit.item.Items.items
import static org.bukkit.Material.*

Items.register 'pickaxe-template', {
    nbt.CanDestroy = [
            'minecraft:dirt',
            'minecraft:sand',
            'minecraft:soul_sand',
            'minecraft:grass',
            'minecraft:concrete_powder',
            'minecraft:snow'
    ]
    nbt.HideFlags = 63
}

Items.register 'default', {
    item IRON_PICKAXE
    apply items['pickaxe-template']
    text """
    §bКирка

    Классическая кирка. Идеальна
    для начинающего, ничего лишнего.
    """
}

Items.register 'professional', {
    item DIAMOND_PICKAXE
    apply items['pickaxe-template']
    text """
    §bПрофессиональная кирка

    Кирка для настоящего профи.
    Ломает от 1 до 5 блоков.
    """
}

Items.register 'prestige', {
    item GOLD_PICKAXE
    apply items['pickaxe-template']
    enchant(Enchantment.DIG_SPEED, 2)
    text """
    §bПрестижная кирка

    Самая престижная и элегантная
    кирка для истинного коллекционера.
    Ломает 5 блоков. С вероятностью 50%.
    """
}

Guis.register 'pickaxe', { player ->
    def user = App.app.getUser((Player) player)

    title 'Улучшение кирки'
    layout '----F---X'
    button MuseumGuis.background
    button 'F' icon {
        if (user.pickaxeType.next == null) {
            item CLAY_BALL
            nbt.other = 'tochka'
            text.clear()
            text '§8У вас наилучшая кирка.'
        } else {
            apply items[user.pickaxeType.next.name().toLowerCase()]
        }
        text ''
        text "Цена: $user.pickaxeType.next.price"
    } leftClick {
        performCommand'pickaxe'
    }

    button 'X' icon {
        item BARRIER
        text '§cНазад'
    } leftClick {
        performCommand("gui main")
    }
}