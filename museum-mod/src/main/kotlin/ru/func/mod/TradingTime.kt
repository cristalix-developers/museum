package ru.func.mod

import dev.xdark.clientapi.event.network.PluginMessage
import dev.xdark.feder.NetUtil
import ru.cristalix.uiengine.UIEngine
import ru.cristalix.uiengine.element.TextElement
import ru.cristalix.uiengine.eventloop.animate
import ru.cristalix.uiengine.utility.*

/**
 * @author Рейдж 23.08.2021
 * @project museum
 */
object TradingTime {
    init {
        val body = carved {
            origin = LEFT
            align = LEFT
            size = V3(180.0, 5.0, 0.0)
            color = Color(244, 148, 198, 1.0)
            enabled = false
        }
        val title = text {
            origin = TOP
            align = TOP
            color = WHITE
            shadow = true
            content = "Загрузка..."
            offset.y -= 15
        }
        val cooldown = carved {
            offset = V3(0.0, -50.0)
            origin = BOTTOM
            align = BOTTOM
            size = V3(180.0, 5.0, 0.0)
            color = Color(0, 0, 0, 0.62)
            addChild(body, title)
            enabled = false
        }

        mod.registerHandler<PluginMessage> {
            if (channel == "museum:tradingtime") {
                val text = NetUtil.readUtf8(data)
                val seconds = data.readInt()
                cooldown.enabled = true
                body.enabled = true
                title.content = text
                body.animate(seconds - 0.1) {
                    size.x = 0.0
                }
                UIEngine.schedule(seconds + 0.1) {
                    cooldown.enabled = false
                    body.enabled = false
                    body.size.x = 180.0
                }
            }
        }
        UIEngine.overlayContext.addChild(cooldown)
    }
}