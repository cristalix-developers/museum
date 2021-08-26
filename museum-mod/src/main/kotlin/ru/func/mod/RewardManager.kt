package ru.func.mod

import dev.xdark.clientapi.event.network.PluginMessage
import dev.xdark.clientapi.event.render.RenderTickPre
import dev.xdark.clientapi.item.ItemTools
import dev.xdark.feder.NetUtil
import org.lwjgl.input.Mouse
import org.lwjgl.opengl.Display
import ru.cristalix.uiengine.UIEngine
import ru.cristalix.uiengine.element.AbstractElement
import ru.cristalix.uiengine.element.RectangleElement
import ru.cristalix.uiengine.element.TextElement
import ru.cristalix.uiengine.element.animate
import ru.cristalix.uiengine.utility.*

class RewardManager {

    private var currentDay = 0
    lateinit var hint: RectangleElement
    lateinit var hintText: TextElement

    init {
        val box = rectangle {
            size = V3(1000.0, 1000.0)
            color = Color(0, 0, 0, 0.86)
            origin = CENTER
            align = CENTER
            enabled = false
        }
        val week = arrayListOf<Day>()

        UIEngine.overlayContext.addChild(box)
        UIEngine.registerHandler(PluginMessage::class.java) {
            if (channel == "museum:weekly-reward") {
                currentDay = data.readInt()

                val topText = text {
                    origin = CENTER
                    align = CENTER
                    color = WHITE
                    shadow = true
                    scale = V3(1.5, 1.5)
                    offset.y -= 110.0
                    content = "Ваша ежедневная награда / $currentDay день"
                }

                UIEngine.overlayContext.addChild(topText)

                for (i in 0..6) {
                    val dayBox = Day(
                        i + 1, ItemTools.read(data), NetUtil.readUtf8(data),
                        when {
                            currentDay > i + 1 -> "§7СОБРАНО"
                            currentDay < i + 1 -> "§eСКОРО"
                            else -> "§f§lСОБРАТЬ"
                        }, i + 1 < currentDay
                    )
                    val topElement = dayBox.children[0] as TextElement
                    when {
                        currentDay > i + 1 -> topElement.content = "§7" + topElement.content
                        currentDay < i + 1 -> topElement.content = "§b" + topElement.content
                        else -> {
                            topElement.content = "§lУРА!\nнаграда"
                            dayBox.color = Color(224, 118, 20, 0.3)
                            dayBox.onClick = { _, _, _ ->
                                UIEngine.overlayContext.removeChild(
                                    box,
                                    topElement,
                                    hint,
                                    *week.toTypedArray(),
                                    topText
                                )
                                UIEngine.clientApi.chat().sendChatMessage("/lootboxsound")
                                UIEngine.clientApi.minecraft().setIngameFocus()
                            }
                        }
                    }

                    dayBox.onHover = { element: AbstractElement, hovered: Boolean ->
                        if (hovered) {
                            hintText.content = dayBox.name
                            hint.enabled = true
                            hint.size.x = hintText.size.x + 4
                            week.forEach {
                                if (it != element) {
                                    it.normalize()
                                    it.animate(0.1) {
                                        size.x = 50.0
                                        size.y = 150.0
                                    }
                                }
                                UIEngine.overlayContext.schedule(0.1) {
                                    if (it != dayBox) {
                                        it.move(it.day.compareTo(dayBox.day))
                                    }
                                }
                            }
                            element.animate(0.1) {
                                size.x = 55.0
                                size.y = 175.0
                            }
                        } else {
                            hint.enabled = false
                            week.forEach {
                                if (it != dayBox) {
                                    it.normalize()
                                }
                            }
                            element.animate(0.1) {
                                size.x = 50.0
                                size.y = 150.0
                            }
                        }
                    }
                    week.add(dayBox)
                    UIEngine.overlayContext.addChild(dayBox)
                }
                box.enabled = true
                UIEngine.clientApi.minecraft().setIngameNotInFocus()
            }
        }

        UIEngine.registerHandler(RenderTickPre::class.java) {
            hint.offset.x = (Mouse.getX() / UIEngine.clientApi.resolution().scaleFactor).toDouble()
            hint.offset.y = ((Display.getHeight() - Mouse.getY()) / UIEngine.clientApi.resolution().scaleFactor).toDouble()
        }

        hintText = text {
            content = "???"
            offset.x = 2.0
            offset.y = 2.0
        }
        hint = rectangle {
            color = Color(0, 0, 0, 0.7)
            addChild(hintText)
            size.x = 100.0
            size.y = 14.0
            offset.z += 10
            enabled = false
        }

        UIEngine.overlayContext.addChild(hint)
    }
}