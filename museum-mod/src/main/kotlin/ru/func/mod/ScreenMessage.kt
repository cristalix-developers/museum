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
            align = Relative.CENTER
            origin = Relative.CENTER
            color = TRANSPARENT
            scale = V3(2.1, 2.1)
            enabled = false
        }

        UIEngine.registerHandler(PluginMessage::class.java) {
            if (channel == "museum:screenmessage") {
                item.enabled = true
                item.addChild(item {
                    repeat(2) {
                        stack = ItemTools.read(data)
                    }
                })
            }
        }
        UIEngine.overlayContext.addChild(item)
    }
}