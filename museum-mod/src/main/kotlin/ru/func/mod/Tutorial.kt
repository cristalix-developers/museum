package ru.func.mod

import dev.xdark.clientapi.event.network.PluginMessage
import dev.xdark.feder.NetUtil
import ru.cristalix.clientapi.registerHandler
import ru.cristalix.uiengine.UIEngine
import ru.cristalix.uiengine.utility.*

object Tutorial {

    private val agreeButtonSize = 16.0

    init {
        val welcomeText = text {
            origin = CENTER
            shadow = true
            content = ""
        }

        val buttonAgree = carved {
            carveSize = 1.0
            align = BOTTOM
            origin = CENTER
            size = V3(76.0, agreeButtonSize)
            val normalColor = Color(160, 29, 40, 0.83)
            val hoveredColor = Color(231, 61, 75, 0.83)
            color = normalColor
            onHover {
                animate(0.08, Easings.QUINT_OUT) {
                    color = if (hovered) hoveredColor else normalColor
                    scale = V3(if (hovered) 1.1 else 1.0, if (hovered) 1.1 else 1.0, 1.0)
                }
            }
            onMouseUp {
                close()
                menuStack.clear()
            }
            +text {
                align = CENTER
                origin = CENTER
                color = WHITE
                scale = V3(0.9, 0.9, 0.9)
                content = "Выйти [ ESC ]"
                shadow = true
            }
        }

        val agreeButton = text {
            origin = BOTTOM_RIGHT
            shadow = true
            content = ""
        }

        val box = rectangle {
            align = CENTER
            origin = CENTER
            +welcomeText
            +agreeButton
        }

        UIEngine.overlayContext + box

        mod.registerHandler<PluginMessage> {
            when (channel) {

            }
        }
    }
}