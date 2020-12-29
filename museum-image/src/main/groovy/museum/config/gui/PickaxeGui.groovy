package museum.config.gui

import clepto.bukkit.item.Items
import clepto.bukkit.menu.Guis
import museum.App
import museum.util.MessageUtil
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player

import static clepto.bukkit.item.Items.items
import static org.bukkit.Material.*

Items.register 'pickaxe-template', {
    nbt.CanDestroy = [
            'minecraft:stained_glass',
            'minecraft:dirt',
            'minecraft:sand',
            'minecraft:soul_sand',
            'minecraft:grass',
            'minecraft:concrete_powder',
            'minecraft:concrete',
            'minecraft:stained_hardened_clay',
            'minecraft:snow',
            'minecraft:red_sandstone'
    ]
    nbt.Unbreakable = 1
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
    enchant(Enchantment.DIG_SPEED, 1)
    text """
    §bПрестижная кирка

    Самая престижная и элегантная
    кирка для истинного коллекционера.
    Ломает 5 блоков. С вероятностью 50%.
    """
}

Items.register 'legendary', {
    item DIAMOND_PICKAXE
    apply items['pickaxe-template']
    enchant(Enchantment.DIG_SPEED, 2)
    nbt.prison = '23feb'
    text """
    §b§lЛегендарная кирка

    Абсолютное орудие.
    """
}

Guis.register 'pickaxe', { player ->
    def user = App.app.getUser((Player) player)

    title 'Улучшение кирки'
    layout """
        ----F----
        ---------
        --I-H-K--
        --I-H-K--
        --I-H-K--
        ----X----
    """
    button MuseumGuis.background
    button 'F' icon {
        if (user.pickaxeType.next == null) {
            item CLAY_BALL
            nbt.other = 'tochka'
            text '§8У вас наилучшая кирка.'
        } else {
            apply items[user.pickaxeType.next.name().toLowerCase()]
            text ''
            text "Цена: ${MessageUtil.toMoneyFormat(user.pickaxeType.next.price)}"
        }
    } leftClick {
        performCommand 'pickaxe'
    }

    for (double i = 1; i <= 1.2; i = i + 0.1) {
        def cost = i * 10000d
        if (i <= user.info.extraChance) {
            button 'K' icon {
                item CLAY_BALL
                nbt.other = 'tochka'
                text '§8Данная прокачка уже получена.'
            }
        } else {
            button 'K' icon {
                item BOOK
                text "§b+10% к удаче обнаружния кости"
                text ""
                text "Купить за §e$cost\$"
            } leftClick {
                if (user.money > cost) {
                    user.money = user.money - cost
                    user.info.extraChance = user.info.extraChance + 0.1
                    closeInventory()
                }
            }
        }
    }
    for (int i = 1; i <= 3; i++) {
        def cost = i * 5000
        if (i <= user.info.extraSpeed) {
            button 'H' icon {
                item CLAY_BALL
                nbt.other = 'tochka'
                text '§8Данная прокачка уже получена.'
            }
        } else {
            button 'H' icon {
                item EXP_BOTTLE
                amount i
                text "§b+1 эффективности кирки"
                text ""
                text "Купить за §e$cost\$"
            } leftClick {
                if (user.money > cost) {
                    user.money = user.money - cost
                    user.info.extraSpeed = user.info.extraSpeed + 1
                    closeInventory()
                }
            }
        }
    }

    for (int i = 1; i <= 3; i++) {
        def cost = i * 1000
        if (i * 5 <= user.info.extraBreak) {
            button 'I' icon {
                item CLAY_BALL
                nbt.other = 'tochka'
                text '§8Данная прокачка уже получена.'
            }
        } else {
            button 'I' icon {
                item STONE
                amount i * 5
                text "§b${i * 5} дополнлительных удара(ов)"
                text ""
                text "Купить за §e$cost\$"
            } leftClick {
                if (user.money > cost) {
                    user.money = user.money - cost
                    user.info.extraBreak = user.info.extraBreak + 5
                    closeInventory()
                }
            }
        }
    }

    button 'X' icon {
        item BARRIER
        text '§cНазад'
    } leftClick {
        performCommand("gui tools")
    }
}