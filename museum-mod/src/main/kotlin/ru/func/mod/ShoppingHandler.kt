package ru.func.mod

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dev.xdark.clientapi.ClientApi
import dev.xdark.clientapi.entry.ModMain
import dev.xdark.clientapi.event.lifecycle.GameLoop
import dev.xdark.clientapi.event.network.PluginMessage
import dev.xdark.clientapi.resource.ResourceLocation
import dev.xdark.feder.NetUtil
import ru.cristalix.uiengine.UIEngine
import ru.cristalix.uiengine.UIEngine.listener
import ru.cristalix.uiengine.element.Context3D
import ru.cristalix.uiengine.element.RectangleElement
import ru.cristalix.uiengine.element.animate
import ru.cristalix.uiengine.utility.*
import java.util.*

class ShoppingHandler : ModMain {

    data class ToSell(
            val address: String,
            val title: String,
            val min: V3,
            val max: V3,
            val cost: Double,
            val uuid: UUID
    )

    private lateinit var box: RectangleElement

    override fun load(api: ClientApi) {
        UIEngine.initialize(api)

        val gson = Gson()
        var sell: Array<ToSell>

        box = rectangle {
            offset = Relative.BOTTOM
            align = Relative.TOP
            color = Color(255, 0, 255, 1.0)
            size = V3(100.0, 40.0)
        }

        UIEngine.overlayContext.addChild(box)

        api.messageBus().register(listener, PluginMessage::class.java, {
            if (it.channel == "shop") {
                sell = gson.fromJson(NetUtil.readUtf8(it.data), Array<ToSell>::class.java)
                api.chat().printChatMessage(gson.toJson(sell))
            }
        }, 1)

        /*api.messageBus().register(listener, GameLoop::class.java, {
        }*/
    }

    override fun unload() {
        UIEngine.uninitialize()
    }
}