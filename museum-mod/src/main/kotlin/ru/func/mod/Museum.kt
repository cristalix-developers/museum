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

    private lateinit var shopbox: RectangleElement
    private lateinit var shoptext: TextElement
    private var hints = ArrayList<Pair<Long, AbstractElement>>()

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

        shoptext = text {
            offset = Relative.TOP_LEFT
            align = Relative.TOP_LEFT
            align.x += 0.05
            align.y += 0.05
            color = Color(0, 255, 0, 1.0)
        }

        shopbox = rectangle {
            size = V3(220.0, 40.0)
            offset = V3(0.5, 0.5)
            align = V3(0.33, 0.7)
            color = Color(0, 0, 0, 0.62)
            enabled = false
            addChild(shoptext)
            addChild(text {
                offset = Relative.LEFT
                align = Relative.LEFT
                align.x += 0.05
                align.y += 0.05
                color = Color(255, 255, 255, 1.0)
                content = "Чтобы купить нажмите Enter"
            })
        }
        UIEngine.overlayContext.addChild(shopbox)

        // Подсказки слева
        val help = rectangle {
            size = V3(195.0, 110.0)
            align = Relative.LEFT
            origin = Relative.LEFT
            color = Color(0, 0, 0, 0.62)
            arrayOf(
                "§fГорячие клавиши",
                "",
                "§b§lM§f - §bкарта мира §f㸾",
                "§b§lN§f - §bскрыть/показать §fэто окно 㱬",
                "§b§lH§f - §bответы §fна разные вопросы 㗒",
                "§b§lG§f - §fменю префиксов 䁿",
                "",
                "§bПриятной игры! §f㶅"
            ).forEachIndexed { i, line ->
                addChild(text {
                    content = line
                    align = V3(0.1, 0.12 * i + 0.1, 0.0)
                    origin = V3(0.1, 0.12 * i + 0.1, 0.0)
                    shadow = true
                    color = WHITE
                })
            }
        }
        UIEngine.overlayContext.addChild(help)

        registerChannel("shop") {
            sell = Gson().fromJson(NetUtil.readUtf8(this), Array<ToSell>::class.java)
        }

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
                clientApi.chat().sendChatMessage("/gui excavation")
            } else if (key == Keyboard.KEY_H) {
                clientApi.chat().sendChatMessage("/helps")
            } else if (key == Keyboard.KEY_G) {
                clientApi.chat().sendChatMessage("/prefixes")
            }
        }
    }
}