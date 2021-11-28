package museum.config.gui

import clepto.bukkit.item.Items
import clepto.bukkit.menu.Guis
import museum.App
import museum.client_conversation.AnimationUtil
import museum.player.pickaxe.PickaxeUpgrade
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
        ---RFL---
        ---------
        -E-E-E-E-
        --E-E-X--
    """
    button MuseumGuis.background

    button 'R' icon {
        item CLAY_BALL
        text '§bВаша кирка справа'
        nbt.other = 'arrow_right'
    }

    button 'L' icon {
        item CLAY_BALL
        text '§bВаша кирка слева'
        nbt.other = 'arrow_left'
    }

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

    PickaxeUpgrade.values().each { upgrade ->
        def currentLevel = user.pickaxeImprovements[upgrade]
        def has = currentLevel == upgrade.maxLevel
        button 'E' icon {
            item upgrade.icon
            nbt.HideFlags = 63
            if (has) {
                text '§8У вас максимальный уровень этого улучшения'
                nbt.other = 'tochka'
            } else {
                text """
                $upgrade.title
                §7Цена ${MessageUtil.toMoneyFormat(upgrade.cost)}

                &b${currentLevel + 1} &f➠ &b${currentLevel + 2} уровень &fиз $upgrade.maxLevel &a▲▲▲

                §7$upgrade.lore

                §aНажмите чтобы улучшить
                """
                def pair = upgrade.nbt.split(':')
                nbt(pair[0], pair[1])
            }
        } leftClick {
            if (user.money >= upgrade.cost && !has) {
                user.giveMoney(-upgrade.cost)
                user.pickaxeImprovements.replace(upgrade, currentLevel + 1)
                AnimationUtil.glowing(user, 0, 0, 255)
                Guis.open(player, 'pickaxe', player)
            } else {
                AnimationUtil.buyFailure user
            }
        }
    }

    button 'O' icon {
        item PAPER
        text """
        §bУлучшение кирки недоступно
        
        Данная система будет 
        изменена к концу месяца.
        """
    }

    button 'X' icon {
        item CLAY_BALL
        text '§cНазад'
        nbt.other = "cancel"
    } leftClick {
        performCommand("gui main")
    }
}