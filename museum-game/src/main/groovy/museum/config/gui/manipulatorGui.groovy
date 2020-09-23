package museum.config.gui

import clepto.bukkit.menu.Guis
import museum.App
import museum.museum.map.SkeletonSubjectPrototype
import museum.museum.subject.Allocation
import museum.museum.subject.SkeletonSubject
import museum.museum.subject.Subject
import museum.museum.subject.skeleton.Skeleton
import museum.prototype.Managers
import museum.util.SubjectLogoUtil
import org.bukkit.entity.Player

import static clepto.bukkit.item.Items.*
import static museum.museum.subject.Allocation.Action.*
import static org.bukkit.Material.CLAY_BALL
import static org.bukkit.Material.CONCRETE

register 'lockedSkeleton', {
    item CLAY_BALL
    nbt.other = 'tochka'
    text.clear()
    text '§8???'
}

register 'emptySkeleton', {
    item CLAY_BALL
    nbt.other = 'tochka'
    text """
        &7Нужно собрать как минимум 3 фрагмента,
        &7Чтобы выставить скелет в музей.
    """
}

register 'tooBigSkeleton', {
    nbt.color = 0x505050
    text '&7Этот скелет слишком большой для этой витрины'
}

register 'alreadyPlacedSkeleton', {
    text '&eНажмите, чтобы убрать скелет со стенда'
}

register 'availableSkeleton', {
    nbt.color = 0xAAAAAA
    text '&aНажмите, чтобы поставить скелет на стенд'
}

Guis.register 'manipulator', { player ->
    def user = App.app.getUser((Player) player)
    def abstractSubject = (Subject) context

    title abstractSubject.prototype.title

    def gui = delegate

    gui.layout = '--C-I-D--'
    button MuseumGuis.background

    button 'C' icon {
        item CONCRETE
        data abstractSubject.cachedInfo.color.woolData
        text "»$abstractSubject.cachedInfo.color.chatColor §lИзменить цвет §f«"
    } leftClick {
        Guis.open(delegate, 'colorChange', abstractSubject)
    }

    button 'I' icon {
        // ToDo: Перенести этот DynamicItem на новый API
        def itemStack = abstractSubject.prototype.icon.render()
        item itemStack.type
        data itemStack.durability
        text """
            §a$abstractSubject.prototype.title
            §eДоход: §f$abstractSubject.income
        """
    }

    button 'D' icon {
        item CLAY_BALL
        nbt.other = 'guild_shop'
        text 'Убрать'
    } leftClick {
        def allocation = abstractSubject.allocation
        if (!allocation) return
        allocation.perform PLAY_EFFECTS, HIDE_BLOCKS, HIDE_PIECES
        abstractSubject.allocation = null

        inventory.addItem SubjectLogoUtil.encodeSubjectToItemStack(abstractSubject)
        closeInventory()
    }

    if (abstractSubject instanceof SkeletonSubject) {

        def subject = (SkeletonSubject) abstractSubject

        for (it in Managers.skeleton.toSorted({ a, b -> (a.title <=> b.title) })) {
            def skeleton = user.skeletons.get it
            String key
            if (!skeleton) key = 'lockedSkeleton'
            else if (skeleton.unlockedFragments.size() < 3) key = 'emptySkeleton'
            else if (skeleton.prototype.size > (subject.prototype as SkeletonSubjectPrototype).size) key = 'tooBigSkeleton'
            else if (skeleton == subject.skeleton) key = 'alreadyPlacedSkeleton'
            else key = 'availableSkeleton'
            button 'O' icon {
                if (skeleton != null) {
                    context skeleton
                    apply items.skeleton
                    text '§f'
                }
                apply items[key]
            } leftClick {
                if (key == 'availableSkeleton' || key == 'alreadyPlacedSkeleton') {
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
                    }
                    Guis.open(delegate, 'manipulator', subject)
                }
            }
        }

        button 'O' fillAvailable() icon MuseumGuis.backgroundIcon

        def rows = (Managers.skeleton.size() - 1) / 7 + 1

        if (rows) {
            rows.times { gui.layout += '-OOOOOOO-' }
            gui.layout += '---------'
        }
    }
}