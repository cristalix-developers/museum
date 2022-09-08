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

        val hitCount = text {
            offset = V3(-3.0, -44.0)
            origin = BOTTOM_RIGHT
            shadow = true
            content = ""
            enabled = false
        }

        val income = text {
            offset = V3(-3.0, -34.0)
            origin = BOTTOM_RIGHT
            shadow = true
            content = ""
        }

        val box = rectangle {
            align = BOTTOM_RIGHT
            origin = BOTTOM_RIGHT
            +balanceText
            +online
            +income
            +hitCount
        }

        UIEngine.overlayContext + box

        mod.registerHandler<PluginMessage> {
            when (channel) {
                "museum:balance" -> balanceText.content = "Баланс §a${NetUtil.readUtf8(data)}"
                "museum:online" -> online.content = "Онлайн §b${data.readInt()}"
                "museum:income" -> income.content = "Доход в секунду §e${NetUtil.readUtf8(data)}"
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