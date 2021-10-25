package ru.func.mod

import dev.xdark.clientapi.event.network.PluginMessage
import dev.xdark.clientapi.resource.ResourceLocation
import ru.cristalix.uiengine.UIEngine
import ru.cristalix.uiengine.element.TextElement
import ru.cristalix.uiengine.utility.*
import java.text.DecimalFormat

class CrystalBar {

    init {
        val crystal = rectangle {
            align = TOP
            origin = TOP
            scale = V3(1.0, 1.0)
            color = WHITE
            size = V3(32.0, 32.0)
            enabled = false
            offset.y += 20.0

            textureLocation = ResourceLocation.of(
                "minecraft", "mcpatcher/cit/museum/crystal_item.png"
            )
            addChild(text {
                align = BOTTOM
                origin = BOTTOM
                offset.y += 10
                shadow = true
            })
        }

        UIEngine.overlayContext.addChild(crystal)

        val decimalFormat = DecimalFormat("###,###,###,###,###,###")
        UIEngine.registerHandler(PluginMessage::class.java) {
            if (channel == "museum:cosmo-crystal") {
                crystal.enabled = true
                (crystal.children[0] as TextElement).content = decimalFormat.format(data.readInt())
            } else if (channel == "museum:cosmo-leave")
                crystal.enabled = false
        }
    }
}