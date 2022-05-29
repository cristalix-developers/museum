package ru.func.mod

import com.google.gson.Gson
import dev.xdark.clientapi.event.input.KeyPress
import dev.xdark.clientapi.event.lifecycle.GameLoop
import dev.xdark.clientapi.event.render.*
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

        // Подсказки слева
        val help = carved top@{
            val padding = 6.0
            val width = 181.0 + padding
            size.y = 2 * padding
            size.x = width
            offset.x -= 1
            align = Relative.LEFT
            origin = Relative.LEFT
            color = Color(0, 0, 0, 0.52)
            val hints = +flex {
                flexSpacing = 2.0
                offset.x += padding
                offset.y += padding
                flexDirection = FlexDirection.DOWN
            }
            arrayOf(
                "§fГорячие клавиши",
                "",
                "§b§lM§f - §bкарта мира §f㸾",
                "§b§lN§f - §bскрыть/показать §fэто окно 㱬",
                "§b§lH§f - §bответы §fна разные вопросы 㗒",
                "§b§lG§f - §fменю префиксов 䁿",
                "",
                "§bПриятной игры! §f㶅"
            ).forEach { line ->
                hints + text {
                    content = line
                    size.x = width - padding
                    shadow = true
                    color = WHITE
                    this@top.size.y += lineHeight + hints.flexSpacing
                }
            }
        }
        UIEngine.overlayContext + help

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
                        help.enabled = false
                    }
                }

                if (!shown) {
                    shopbox.enabled = false
                    activeSubject = null
                }
            }
        }

        registerHandler<KeyPress> {
            if (key == Keyboard.KEY_RETURN && activeSubject != null) {
                clientApi.chat().sendChatMessage("/buy ${activeSubject?.address}")
            } else if (key == Keyboard.KEY_N) {
                help.enabled = !help.enabled
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