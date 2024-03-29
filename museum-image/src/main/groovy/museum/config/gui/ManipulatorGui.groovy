package museum.config.gui

import clepto.bukkit.menu.Guis
import me.func.mod.ui.Glow
import me.func.protocol.data.color.GlowColor
import museum.App
import museum.config.command.WagonConfig
import museum.fragment.GemType
import museum.fragment.Meteorite
import museum.multi_chat.ChatType
import museum.multi_chat.MultiChatUtil
import museum.museum.Museum
import museum.museum.map.SkeletonSubjectPrototype
import museum.museum.subject.*
import museum.museum.subject.product.FoodProduct
import museum.museum.subject.skeleton.Skeleton
import museum.prototype.Managers
import museum.util.BannerUtil
import museum.util.MessageUtil
import museum.util.SubjectLogoUtil
import org.bukkit.entity.Player
import ru.cristalix.core.formatting.Formatting

import java.text.DecimalFormat

import static clepto.bukkit.item.Items.items
import static clepto.bukkit.item.Items.register
import static museum.museum.map.SubjectType.SKELETON_CASE
import static museum.museum.subject.Allocation.Action.*
import static org.bukkit.Material.*

def num = new DecimalFormat('###,###,###,###,###,###.##')

register 'lockedSkeleton', {
    item CLAY_BALL
    nbt.other = 'tochka'
    text.clear()
    text '§8???'
}

register 'emptySkeleton', {
    item BONE
    nbt.color = 0xFFAAAAAA
    text """
        &7Нужно собрать как минимум 3 фрагмента,
        &7Чтобы выставить скелет в музей.
    """
}

register 'tooBigSkeleton', {
    nbt.color = 0xFFAAAAAA
    text '&cВитрина слишком маленькая'
}

register 'alreadyPlacedSkeleton', {
    nbt.color = 0xFFAAAAAA
    text '&eКости на другой витрине'
}

register 'currentSkeleton', {
    nbt.glow_color = 0xFF55FF55
    text '&cУбрать скелет со стенда'
}

register 'availableSkeleton', {
    text '&aПоставить скелет на стенд'
}

Guis.register 'manipulator', { player ->
    def user = App.app.getUser((Player) player)
    Subject abstractSubject
    try {
        abstractSubject = user.getSubject(UUID.fromString(context as String))
    } catch (Exception ignored) {
        return
    }
    if (!abstractSubject)
        return

    title abstractSubject.prototype.title

    def gui = delegate

    if (abstractSubject instanceof RelicShowcaseSubject) {
        gui.layout = 'XXXXOXXXE'
        def subject = (RelicShowcaseSubject) abstractSubject

        button 'X' icon {
            item STAINED_GLASS_PANE
            text '&7Вставьте реликвию'
        } fillAvailable()
        if (subject.fragment) {
            def type = subject.fragment.address
            button 'O' icon {
                if (type.contains("meteor")) {
                    def meteor = Meteorite.Meteorites.valueOf(type.split("\\_")[1].toUpperCase())
                    item subject.fragment.item.type
                    text meteor.title
                    text ""
                    subject.fragment.getItem().getItemMeta().lore.forEach {text it }
                } else if (type.contains(":")) {
                    item CLAY_BALL
                    nbt.museum = GemType.valueOf(type.split(':')[0]).texture
                } else {
                    apply items['relic-' + type]
                }
                text ''
                text '&7Нажмите чтобы снять'
            } leftClick {
                closeInventory()
                // Так выглядит паранойя
                def subjectRelic = subject.fragment
                subject.setFragment(null)
                user.getInventory().addItem(subjectRelic.item)
                user.relics.put(subjectRelic.uuid, subjectRelic)
                subject.updateFragment()
                ((Museum) user.state).updateIncrease()
                BannerUtil.updateBanners(subject)
                user.updateIncome()
                MessageUtil.find 'relic-tacked' send user
            }
        }
        button 'E' icon {
            item CLAY_BALL
            nbt.other = "cancel"
            text '&cУбрать витрину'
        } leftClick {
            def allocation = abstractSubject.allocation
            if (!allocation) return
            allocation.perform PLAY_EFFECTS, HIDE_BLOCKS, HIDE_PIECES, DESTROY_DISPLAYABLE
            BannerUtil.deleteBanners(abstractSubject)
            abstractSubject.owner.updateIncome()
            abstractSubject.allocation = null

            inventory.addItem SubjectLogoUtil.encodeSubjectToItemStack(abstractSubject)
            closeInventory()
        }
        return
    }

    gui.layout = '--C-I-D--'
    button MuseumGuis.background

    button 'C' icon {
        item CONCRETE
        data abstractSubject.cachedInfo.color.woolData
        text "§bИзменить цвет"
    } leftClick {
        Guis.open(delegate, 'colorChange', abstractSubject.cachedInfo.uuid)
    }

    button 'D' icon {
        item CLAY_BALL
        nbt.other = 'guild_shop'
        text '§cУбрать'
    } leftClick {
        def allocation = abstractSubject.allocation
        if (!allocation) return
        allocation.perform PLAY_EFFECTS, HIDE_BLOCKS, HIDE_PIECES, DESTROY_DISPLAYABLE
        BannerUtil.deleteBanners(abstractSubject)
        abstractSubject.owner.updateIncome()
        abstractSubject.allocation = null

        inventory.addItem SubjectLogoUtil.encodeSubjectToItemStack(abstractSubject)
        closeInventory()
    }

    if (abstractSubject instanceof SkeletonSubject) {

        def subject = (SkeletonSubject) abstractSubject

        for (it in Managers.skeleton.toSorted({ a, b -> (a.title <=> b.title) })) {

            def skeleton = user.skeletons.get it

            def placedOn = user.museums.get(Managers.museum.getPrototype('main')).getSubjects(SKELETON_CASE)
                    .find {it.skeleton && it.skeleton == skeleton}

            String key
            if (!skeleton) key = 'lockedSkeleton'
            else if (skeleton.unlockedFragments.size() < 3) key = 'emptySkeleton'
            else if (skeleton.prototype.size > (subject.prototype as SkeletonSubjectPrototype).size) key = 'tooBigSkeleton'
            else if (skeleton == subject.skeleton) key = 'currentSkeleton'
            else if (skeleton == placedOn?.skeleton) key = 'alreadyPlacedSkeleton'
            else key = 'availableSkeleton'


            button 'O' icon {
                if (skeleton != null) {
                    context skeleton
                    apply items.skeleton
                    text '§f'
                }
                apply items[key]
            } leftClick {
                if (key == 'availableSkeleton' || key == 'currentSkeleton') {
                    Skeleton previousSkeleton = subject.skeleton
                    Allocation allocation = subject.allocation
                    if (allocation) {
                        if (previousSkeleton) {
                            allocation.perform HIDE_PIECES
                            allocation.removePiece previousSkeleton.getPrototype()
                        }
                        if (subject.skeleton == skeleton)
                            subject.skeleton = null
                        else {
                            user.subjects.each {
                                if (it instanceof SkeletonSubject && it.skeleton == skeleton) {
                                    ((SkeletonSubject) it).skeleton = null
                                    ((SkeletonSubject) it).updateSkeleton true
                                    if (it.allocation) {
                                        it.allocation.perform HIDE_PIECES
                                        it.allocation.removePiece skeleton.getPrototype()
                                    }
                                }
                            }
                            subject.skeleton = skeleton
                        }
                        subject.updateSkeleton true
                        user.updateIncome()
                    }
                    Guis.open(delegate, 'manipulator', subject.cachedInfo.uuid)
                }
            }
        }

        button 'O' fillAvailable() icon MuseumGuis.backgroundIcon

        def rows = (Managers.skeleton.size() - 1) / 7 + 1

        def upgradeCost = 10000
        def upgradePercent = 20

        if (rows) {
            rows.times { gui.layout += '-OOOOOOO-' }
            gui.layout += '----P----'
            if (subject.level < 50) {
                button 'P' icon {
                    item CLAY_BALL
                    nbt.other = 'guild_invite'
                    text """
                    &aУлучшить витрину

                    &fЦена улучшения &a10'000 \$

                    С каждым уровнем витрина 
                    приносит на &b$upgradePercent%▲&f больше дохода
                    &b${subject.level} &fуровень ➠ &b&l${subject.level + 1} уровень &a+${subject.level * upgradePercent}% ▲▲▲
                    """
                } leftClick {
                    if (user.money >= upgradeCost) {
                        user.giveMoney(-upgradeCost)
                        subject.level = subject.level + 1
                        Glow.animate(user.handle(), 0.4, GlowColor.GREEN)
                        MultiChatUtil.sendMessage(user.getPlayer(), ChatType.SYSTEM, Formatting.fine("Вы улучшили витрину до §b$subject.level§f уровня!"))
                        Guis.open(delegate, 'manipulator', abstractSubject.cachedInfo.uuid)
                        BannerUtil.updateBanners(subject)
                        user.updateIncome()
                    } else {
                        MessageUtil.find('nomoney').send(user)
                        closeInventory()
                    }
                }
            } else {
                button 'P' icon {
                    item CLAY_BALL
                    nbt.other = 'guild_invite'
                    text """
                    &7Витрина максимального уровня
                    """
                }
            }
        }
    } else if (abstractSubject instanceof StallSubject) {
        if (abstractSubject.food.isEmpty()) {
            button 'I' icon {
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
        (FoodProduct.values().length / 9).times { gui.layout += 'OOOOOOOOO' }
        def summary = 0
        abstractSubject.food.forEach { key, value ->
            def term = value * key.cost
            summary = summary + term
            button 'O' icon {
                item SLIME_BALL
                text "x$value $key.name &e=$term\$"
            }
        }
        button 'I' icon {
            def itemStack = abstractSubject.prototype.icon
            item itemStack.type
            data itemStack.durability
            text """
            §a$abstractSubject.prototype.title
            §fТоваров на §e$summary\$
            """
        }
    }

    if (abstractSubject instanceof SkeletonSubject) {
        button 'I' icon {
            def itemStack = abstractSubject.prototype.icon
            item itemStack.type
            data itemStack.durability
            text """
            §a$abstractSubject.prototype.title
            §eДоход: §f${num.format(abstractSubject.income)}
            """
        }
    }
}