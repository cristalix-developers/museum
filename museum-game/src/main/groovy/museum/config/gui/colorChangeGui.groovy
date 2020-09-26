package museum.config.gui

import clepto.bukkit.menu.Guis
import museum.museum.subject.Allocation
import museum.museum.subject.Subject
import org.bukkit.Material
import ru.cristalix.core.formatting.Color

import static museum.util.Colorizer.applyColor

Guis.register 'colorChange', {

    def subject = (Subject) context
    def allocation = subject.allocation

    title 'Изменение цвета'

    layout '''
        ----X----
        -CCCCCCC-
        -CCCCCCC-
        ---------
    '''

    button MuseumGuis.background
    button MuseumGuis.backToManipulator(subject)

    Color.values().each {color ->
        button 'C' icon {
            item Material.CONCRETE
            data color.woolData
            text """Выбрать $color.chatColor${color.teamName.replace('ые', 'ый').replace('ие', 'ий')} &fцвет"""
        } leftClick {
            subject.cachedInfo.color = color
            if (allocation) {
                allocation.prepareUpdate(data -> applyColor(data, color))
                allocation.perform(Allocation.Action.UPDATE_BLOCKS)
            }
        }
    }
}
