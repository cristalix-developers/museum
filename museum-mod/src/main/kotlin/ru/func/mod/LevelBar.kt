package ru.func.mod

import dev.xdark.clientapi.event.network.PluginMessage
import ru.cristalix.uiengine.UIEngine
import ru.cristalix.uiengine.element.RectangleElement
import ru.cristalix.uiengine.element.TextElement
import ru.cristalix.uiengine.element.animate
import ru.cristalix.uiengine.utility.*

/**
 * @author Рейдж 08.08.2021
 * @project museum
 */
class LevelBar {
    init {
        val online = rectangle {
            offset = V3(0.0, -25.0)
            origin = BOTTOM
            align = BOTTOM
            size = V3(180.0, 5.0, 0.0)
            color = Color(0, 0, 0, 0.62)
            addChild(
                rectangle {
                    origin = LEFT
                    align = LEFT
                    size = V3(0.0, 5.0, 0.0)
                    color = Color(42, 102, 189, 1.0)
                },
                text {
                    origin = TOP
                    align = TOP
                    color = WHITE
                    shadow = true
                    content = "Загрузка... "
                    offset.y -= 15
                }
            )
        }

        UIEngine.overlayContext.addChild(online)

        UIEngine.registerHandler(PluginMessage::class.java) {
            if (channel == "museum:levelbar") {
                val level = data.readInt()
                val experience = data.readInt()
                val requiredExperience = data.readInt()

                (online.children[0] as RectangleElement).animate(1) {
                    size.x = 180.0 / requiredExperience * experience
                }

                (online.children[1] as TextElement).content = "Уровень §b${level} §7$experience из $requiredExperience"
            }
        }
    }
}