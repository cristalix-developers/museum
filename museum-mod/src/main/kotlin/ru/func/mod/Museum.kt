package ru.func.mod

import com.google.gson.Gson
import dev.xdark.clientapi.event.input.KeyPress
import dev.xdark.clientapi.event.lifecycle.GameLoop
import dev.xdark.clientapi.event.network.PluginMessage
import dev.xdark.clientapi.event.render.*
import dev.xdark.clientapi.item.ItemTools
import dev.xdark.clientapi.opengl.GlStateManager
import dev.xdark.feder.NetUtil
import org.lwjgl.input.Keyboard
import ru.cristalix.clientapi.KotlinMod
import ru.cristalix.uiengine.UIEngine
import ru.cristalix.uiengine.element.*
import ru.cristalix.uiengine.eventloop.animate
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

        RewardManager()
        LevelBar()
        Statistic()
        ScreenMessage()
        TradingTime()
        CrystalBar()
        Title()

        val minecraft = clientApi.minecraft()

        // Отменяю рендер голода
        registerHandler<HungerRender> {
            isCancelled = true
        }

        registerHandler<ArmorRender> {
            isCancelled = true
        }

        registerHandler<HealthRender> {
            isCancelled = true
        }

        registerHandler<ExpBarRender> {
            isCancelled = true
        }

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

        // Уведомлени под курсором
        val timeLife = 5 * 1000

        // Предмет на экране
        val title = text {
            content = ""
            beforeRender = {
                GlStateManager.disableDepth()
            }
            align = V3(0.5, 0.6)
            origin = BOTTOM
            scale = V3(0.0, 0.0, 0.0)
            shadow = true
        }
        val subtitle = text {
            content = ""
            align = V3(0.5, 0.6)
            afterRender = {
                GlStateManager.enableDepth()
            }
            offset.y = 1.0
            origin = TOP
            shadow = true
        }
        UIEngine.overlayContext.addChild(title, subtitle)

        // Сообщения сверху
        val topmessage = rectangle {
            size = V3(clientApi.resolution().scaledWidth_double, clientApi.resolution().scaledHeight_double)
            align = Relative.CENTER
            origin = Relative.CENTER
            addChild(rectangle {
                size = V3(clientApi.resolution().scaledWidth_double, 0.0)
                align = Relative.TOP
                origin = Relative.TOP
                color = Color(0, 0, 0, 0.6)
                offset = V3(0.0, -20.0)
                addChild(text {
                    align = V3(0.5, 0.75)
                    origin = Relative.CENTER
                    scale = V3(1.3, 1.3)
                    enabled = false
                })
            })
        }
        UIEngine.overlayContext.addChild(topmessage)

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

        ru.cristalix.clientapi.registerHandler<PluginMessage> {
            if (channel == "shop")
                sell = Gson().fromJson(NetUtil.readUtf8(data), Array<ToSell>::class.java)
            else if (channel == "museumcursor") {
                val hint = rectangle {
                    offset = Relative.CENTER
                    align = Relative.CENTER
                    scale = V3(1.0, 1.0, 1.0)
                    addChild(text {
                        offset.x -= 20
                        scale = V3(1.0, 1.0, 1.0)
                        content = NetUtil.readUtf8(data)
                        color = Color(255, 255, 255, 1.0)
                    })
                }
                UIEngine.overlayContext.addChild(hint)
                hint.animate(timeLife / 1000, Easings.SINE_BOTH) {
                    offset.x += 70 * (Math.random() - 0.5)
                    offset.y += 70 * Math.random()
                }
                hints.add(Pair(System.currentTimeMillis(), hint))
            } else if (channel == "itemtitle") {
                clientApi.overlayRenderer().displayItemActivation(ItemTools.read(data))
                title.content = NetUtil.readUtf8(data)
                subtitle.content = NetUtil.readUtf8(data)
                title.animate(4.0, Easings.ELASTIC_OUT) {
                    scale.x = 4.0
                    scale.y = 4.0
                }
                subtitle.animate(2, Easings.ELASTIC_OUT) {
                    scale.x = 2.0
                    scale.y = 2.0
                }
                UIEngine.schedule(8) {
                    title.animate(0.25) {
                        scale.x = 0.0
                        scale.y = 0.0
                    }
                    subtitle.animate(0.25) {
                        scale.x = 0.0
                        scale.y = 0.0
                    }
                }
            } else if (channel == "museumcast") {
                topmessage.size =
                    V3(clientApi.resolution().scaledWidth_double, clientApi.resolution().scaledHeight_double)

                val localBox = topmessage.children[0] as RectangleElement
                val message = localBox.children[0] as TextElement

                localBox.size = V3(clientApi.resolution().scaledWidth_double, localBox.size.y)
                message.content = NetUtil.readUtf8(data)
                message.enabled = true

                localBox.animate(5, Easings.BACK_OUT) {
                    size.y = 60.0
                }
                UIEngine.schedule(7) {
                    localBox.animate(5, Easings.BACK_OUT) {
                        size.y = 0.0
                    }
                }
            } else if (channel == "museum:glow") {
                GlowEffect.show(0.5, data.readInt(), data.readInt(), data.readInt(), 1.0)
            }
        }

        registerHandler<GameLoop> {
            // Уведомления под курсором
            val time = System.currentTimeMillis()

            hints.removeIf {
                val remove = time - it.first > timeLife

                if (remove)
                    UIEngine.overlayContext.removeChild(it.second)

                return@removeIf remove
            }

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