package ru.func.mod

import dev.xdark.clientapi.event.network.PluginMessage
import dev.xdark.clientapi.item.Item
import dev.xdark.clientapi.item.ItemTools
import dev.xdark.clientapi.render.model.ItemCameraTransforms
import ru.cristalix.uiengine.UIEngine
import ru.cristalix.uiengine.utility.*

/**
 * @author Рейдж 10.08.2021
 * @project museum
 */
class ScreenMessage {
    init {
        val item = rectangle {
            size = V3(50.0, 50.0)
            align = Relative.CENTER
            origin = Relative.CENTER
            color = TRANSPARENT
            textureSize = V2(50.0, 50.0)
            enabled = false
        }

        UIEngine.overlayContext.addChild(item)

        UIEngine.registerHandler(PluginMessage::class.java) {
            if (channel == "museum:screenmessage") {
                item.enabled = true
                item.addChild(item {
                    UIEngine.clientApi.itemRegistry().getItem(data.readInt()).newStack(1,0)
                })
            }
        }
    }
}