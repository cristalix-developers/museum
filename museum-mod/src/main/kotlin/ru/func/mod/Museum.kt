package ru.func.mod

import com.google.gson.Gson
import dev.xdark.clientapi.event.input.KeyPress
import dev.xdark.clientapi.event.lifecycle.GameLoop
import dev.xdark.clientapi.event.render.*
import dev.xdark.clientapi.gui.ingame.ChatScreen
import dev.xdark.feder.NetUtil
import org.lwjgl.input.Keyboard
import ru.cristalix.clientapi.KotlinMod
import ru.cristalix.uiengine.UIEngine
import ru.cristalix.uiengine.element.*
import ru.cristalix.uiengine.utility.*
import java.util.*

lateinit var mod: Museum

class Museum : KotlinMod() {

    data class ToSell(
        val address: String,
        val title: String,
        val min: V3,
        val max: V3,
        val cost: Double,
        val uuid: UUID,
    )

    private lateinit var shopbox: CarvedRectangle
    private lateinit var shoptext: TextElement

    override fun onEnable() {
        UIEngine.initialize(this)

        mod = this

        RewardManager
        LevelBar
        Statistic
        TradingTime
        CrystalBar

        val minecraft = clientApi.minecraft()

        // Магазин
        var sell: Array<ToSell>? = null
        var activeSubject: ToSell? = null

        shopbox = carved {
            size = V3(210.0, 40.0)
            align = BOTTOM
            origin = BOTTOM
            offset.y -= 65.0
            color = Color(0, 0, 0, 0.52)
            enabled = false
            shoptext = +text {
                origin = TOP
                align = TOP
                offset.y += 4.0
                color = Color(0, 255, 0, 1.0)
            }
            +text {
                origin = BOTTOM
                align = BOTTOM
                offset.y -= 4.0
                color = Color(255, 255, 255, 1.0)
                content = "Чтобы купить нажмите Enter"
            }
        }
        UIEngine.overlayContext + shopbox

        val gson = Gson()

        registerChannel("shop") { sell = gson.fromJson(NetUtil.readUtf8(this), Array<ToSell>::class.java) }

        registerHandler<GameLoop> {
            // Магазин
            val pos = minecraft.mouseOver.pos
            if (pos != null) {
                val x = pos.x
                val y = pos.y
                val z = pos.z

                var shown = false

                sell?.forEach {
                    val a = it.min
                    val b = it.max
                    if (a.x <= x && x <= b.x && a.y <= y && y <= b.y && a.z <= z && z <= b.z) {
                        shoptext.content = "${it.title} ${it.cost.toInt()}$"
                        activeSubject = it
                        shopbox.enabled = true
                        shown = true
                    }
                }

                if (!shown) {
                    shopbox.enabled = false
                    activeSubject = null
                }
            }
        }

        registerHandler<KeyPress> {
            if (UIEngine.clientApi.minecraft().currentScreen() is ChatScreen)
                return@registerHandler
            if (key == Keyboard.KEY_RETURN && activeSubject != null) {
                clientApi.chat().sendChatMessage("/buy ${activeSubject?.address}")
            } else if (key == Keyboard.KEY_M) {
                clientApi.chat().sendChatMessage("/excavationmenu")
            } else if (key == Keyboard.KEY_H) {
                clientApi.chat().sendChatMessage("/helps")
            } else if (key == Keyboard.KEY_G) {
                clientApi.chat().sendChatMessage("/prefixes")
            }
        }
    }
}