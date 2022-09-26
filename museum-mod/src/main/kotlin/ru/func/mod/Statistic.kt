package ru.func.mod

import dev.xdark.clientapi.event.network.PluginMessage
import dev.xdark.feder.NetUtil
import ru.cristalix.clientapi.registerHandler
import ru.cristalix.uiengine.UIEngine
import ru.cristalix.uiengine.utility.*

/**
 * @author Рейдж 08.08.2021
 * @project museum
 */
object Statistic {

    init {

        val hitCount = text {
            offset = V3(-3.0, -14.0)
            origin = BOTTOM_RIGHT
            shadow = true
            content = ""
            enabled = false
        }

        val box = rectangle {
            align = BOTTOM_RIGHT
            origin = BOTTOM_RIGHT
            +hitCount
        }

        UIEngine.overlayContext + box

        mod.registerHandler<PluginMessage> {
            when (channel) {
                "museum:hitcount" -> {
                    val hit = data.readInt()
                    if (hit > 0) {
                        hitCount.enabled = true
                        hitCount.content = "Ударов $hit осталось"
                    } else {
                        hitCount.enabled = false
                    }
                }
            }
        }
    }
}