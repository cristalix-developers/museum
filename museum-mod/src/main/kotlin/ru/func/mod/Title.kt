package ru.func.mod

import dev.xdark.clientapi.event.network.PluginMessage
import dev.xdark.feder.NetUtil
import ru.cristalix.clientapi.registerHandler
import ru.cristalix.uiengine.UIEngine
import ru.cristalix.uiengine.element.TextElement
import ru.cristalix.uiengine.eventloop.animate
import ru.cristalix.uiengine.utility.*

class Title {

    private val message = rectangle {
        origin = CENTER
        align = CENTER
        size = V3(400.0, 250.0, 0.0)
        addChild(text {
            origin = CENTER
            align = CENTER
            size = V3(400.0, 100.0, 0.0)
            color = Color(0, 0, 0, 0.2)
            shadow = true
        })
        enabled = false
    }

    init {
        UIEngine.overlayContext.addChild(message)

        registerHandler<PluginMessage> {
            if (channel == "museum:title") {
                val text = message.children[0] as TextElement
                text.content = NetUtil.readUtf8(data)
                message.enabled = true
                text.animate(0.3) {
                    color.red = 255
                    color.green = 140
                    color.blue = 185
                    color.alpha = 1.0
                    scale.x = 2.2
                    scale.y = 2.2
                }
                UIEngine.schedule(3.1) {
                    text.animate(3.15) {
                        scale.x = 20.0
                        scale.y = 20.0
                        color.alpha = 0.0
                    }
                }
                UIEngine.schedule(3.3) {
                    message.enabled = false
                    text.color.red = 0
                    text.color.green = 0
                    text.color.blue = 0
                    text.color.alpha = 0.0
                    text.scale.x = 1.0
                    text.scale.y = 1.0
                }
            }
        }
    }

}