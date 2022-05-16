package ru.func.mod

import dev.xdark.clientapi.resource.ResourceLocation
import ru.cristalix.uiengine.UIEngine
import ru.cristalix.uiengine.element.TextElement
import ru.cristalix.uiengine.utility.*
import sun.security.jgss.GSSToken.readInt
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
        mod.registerChannel("museum:cosmo-crystal") {
            crystal.enabled = true
            (crystal.children[0] as TextElement).content = decimalFormat.format(readInt())
        }
        mod.registerChannel("museum:cosmo-leave") {
            crystal.enabled = false
        }
    }
}