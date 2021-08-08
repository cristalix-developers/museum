package ru.func.mod

import dev.xdark.clientapi.event.network.PluginMessage
import dev.xdark.feder.NetUtil
import ru.cristalix.uiengine.UIEngine
import ru.cristalix.uiengine.element.animate
import ru.cristalix.uiengine.utility.*
import kotlin.math.absoluteValue

/**
 * @author Рейдж 08.08.2021
 * @project museum
 */
class Statistic {
    init {

        val balanceText = text {
            offset = V3(-3.0, -14.0)
            origin = BOTTOM_RIGHT
            shadow = true
            content = ""
        }

        val online = text {
            offset = V3(-3.0, -24.0)
            origin = BOTTOM_RIGHT
            shadow = true
            content = ""
        }

        val coinPrice = text {
            offset = V3(-3.0, -34.0)
            origin = BOTTOM_RIGHT
            shadow = true
            content = ""
        }

        val hitCount = text {
            offset = V3(-3.0, -44.0)
            origin = BOTTOM_RIGHT
            shadow = true
            content = ""
            enabled = false
        }

        val box = rectangle {
            color = Color(0,0,0,0.62)
            align = Relative.BOTTOM_RIGHT
            origin = BOTTOM_RIGHT
            addChild(balanceText, online, coinPrice, hitCount)
        }

        UIEngine.overlayContext.addChild(box)

        UIEngine.registerHandler(PluginMessage::class.java) {
            when (channel) {
                "museum:balance" -> balanceText.content = "Баланс §a${NetUtil.readUtf8(data)}"
                "museum:online" -> online.content = "Онлайн §b${data.readInt()}"
                "museum:coinprice" -> coinPrice.content = "Цена монеты §e${data.readDouble()}"
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