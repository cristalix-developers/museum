package ru.func.mod

import dev.xdark.clientapi.event.network.PluginMessage
import ru.cristalix.clientapi.registerHandler
import ru.cristalix.uiengine.UIEngine
import ru.cristalix.uiengine.element.CarvedRectangle
import ru.cristalix.uiengine.element.RectangleElement
import ru.cristalix.uiengine.element.TextElement
import ru.cristalix.uiengine.eventloop.animate
import ru.cristalix.uiengine.utility.*

/**
 * @author Рейдж 08.08.2021
 * @project museum
 */
object LevelBar {
    lateinit var progress: CarvedRectangle
    lateinit var hint: TextElement

    init {
        val online = carved {
            offset = V3(0.0, -25.0)
            origin = BOTTOM
            align = BOTTOM
            size = V3(180.0, 5.0, 0.0)
            color = Color(0, 0, 0, 0.62)
            progress = +carved {
                origin = LEFT
                align = LEFT
                size = V3(0.0, 5.0, 0.0)
                color = Color(42, 102, 189, 1.0)
            }
            hint = +text {
                origin = TOP
                align = TOP
                color = WHITE
                shadow = true
                content = "Загрузка... "
                offset.y -= 15
            }
        }

        UIEngine.overlayContext + online

        mod.registerHandler<PluginMessage> {
            if (channel == "museum:levelbar") {
                val level = data.readInt()
                val experience = data.readInt()
                val requiredExperience = data.readInt()

                progress.animate(1) { size.x = 180.0 / requiredExperience * experience }

                hint.content = "Уровень §b${level} §7$experience из $requiredExperience"
            }
        }
    }
}