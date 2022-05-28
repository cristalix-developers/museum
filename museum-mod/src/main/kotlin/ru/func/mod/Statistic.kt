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

        repeat(4) {
            box.children.add(
                rectangle {
                    color = TRANSPARENT
                }
            )
        }

        registerHandler<PluginMessage> {
            when (channel) {
                "museum:balance" -> balanceText.content = "Баланс §a${NetUtil.readUtf8(data)}"
                "museum:online" -> online.content = "Онлайн §b${data.readInt()}"
                "museum:coinprice" -> coinPrice.content = "Цена монеты §e${NetUtil.readUtf8(data)}"
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