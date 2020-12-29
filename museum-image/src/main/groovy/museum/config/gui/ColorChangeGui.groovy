package museum.config.gui

import clepto.bukkit.menu.Guis
import museum.App
import museum.museum.subject.Allocation
import museum.museum.subject.FountainSubject
import org.bukkit.Material
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer
import ru.cristalix.core.formatting.Color

import static museum.util.Colorizer.applyColor

Guis.register 'colorChange', { player ->

    def subject
    try {
        subject = App.app.getUser(player as CraftPlayer).getSubject(UUID.fromString(context as String))
    } catch (Exception ignored) {
        return
    }
    def allocation = subject.allocation

    title 'Изменение цвета'

    layout '''
        ----X----
        -CCCCCCC-
        -CCCCCCC-
        ---------
    '''

    button MuseumGuis.background
    button MuseumGuis.backToManipulator(subject.cachedInfo.uuid)

    Color.values().each {color ->
        button 'C' icon {
            item Material.CONCRETE
            data color.woolData
            text '''Выбрать $color.chatColor${color.teamName.replace('ые', 'ый').replace('ие', 'ий')} &fцвет'''
        } leftClick {
            subject.cachedInfo.color = color
            if (allocation) {
                allocation.prepareUpdate(data -> applyColor(data, color))
                allocation.perform(Allocation.Action.UPDATE_BLOCKS)
            }
            if (subject instanceof FountainSubject) {
                subject.setAllocation(subject.getAllocation())
            }
        }
    }
}
