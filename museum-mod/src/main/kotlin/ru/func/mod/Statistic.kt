package ru.func.mod

import com.google.gson.Gson
import dev.xdark.clientapi.event.lifecycle.GameLoop
import dev.xdark.clientapi.event.network.PluginMessage
import dev.xdark.feder.NetUtil
import org.lwjgl.input.Mouse
import org.lwjgl.opengl.Display
import ru.cristalix.uiengine.UIEngine
import ru.cristalix.uiengine.element.AbstractElement
import ru.cristalix.uiengine.utility.*
import java.nio.file.Files
import java.nio.file.Paths

/**
 * @author Рейдж 08.08.2021
 * @project museum
 */
class Statistic {

    var dragging: Boolean = false
    var draggingX: Double = 0.0
    var draggingY: Double = 0.0
    val gson = Gson()

    fun getMouse(): V2 {
        val resolution = UIEngine.clientApi.resolution()
        val factor = resolution.scaleFactor
        val mouseX = (Mouse.getX() / factor).toDouble()
        val mouseY = ((Display.getHeight() - Mouse.getY()) / factor).toDouble()
        return V2(mouseX, mouseY)
    }

    init {
        val balanceText = text {
            origin = BOTTOM_RIGHT
            shadow = true
            content = ""
        }

        val online = text {
            origin = BOTTOM_RIGHT
            shadow = true
            content = ""
        }

        val coinPrice = text {
            origin = BOTTOM_RIGHT
            shadow = true
            content = ""
        }

        val hitCount = text {
            origin = BOTTOM_RIGHT
            shadow = true
            content = ""
            enabled = false
        }

        val box = rectangle {
            color = Color(0, 0, 0, 0.62)
            onClick = onClick@{ _: AbstractElement, b: Boolean, _: MouseButton ->
                dragging = b
                if (b) {
                    val mouse = getMouse()
                    val resolution = UIEngine.clientApi.resolution()
                    draggingX =
                        mouse.x - this.offset.x - this.align.x * resolution.scaledWidth_double + this.origin.x * this.size.x
                    draggingY =
                        mouse.y - this.offset.y - this.align.y * resolution.scaledHeight_double + this.origin.y * this.size.y
                } else {
                    saveSettings(Settings(align.x, align.y, offset.x, offset.y, 1.0, "normal", true))
                }
            }
            addChild(hitCount, online, coinPrice, balanceText)
        }

        UIEngine.registerHandler(GameLoop::class.java) {
            if (dragging) {
                val resolution = UIEngine.clientApi.resolution()
                val factor = resolution.scaleFactor
                val mouse = getMouse()

                val screenWidth = resolution.scaledWidth_double
                val screenHeight = resolution.scaledHeight_double
                val px = (mouse.x - draggingX) / (screenWidth - box.size.x)
                val py = (mouse.y - draggingY) / (screenHeight - box.size.y)
                val alignX = when {
                    px < 0.33 -> 0.0
                    px > 0.66 -> 1.0
                    else -> 0.5
                }
                val alignY = when {
                    py < 0.33 -> 0.0
                    py > 0.66 -> 1.0
                    else -> 0.5
                }

                box.align = V3(alignX, alignY)
                box.origin = V3(alignX, alignY)
                box.offset.x =
                    ((mouse.x - draggingX + (box.size.x - screenWidth) * alignX)
                        .coerceIn(-alignX * screenWidth, (-alignX + 1) * screenWidth) * factor).toInt().toDouble() /
                            factor + if (alignX == 0.5) 0.5 else 0.0
                box.offset.y =
                    ((mouse.y - draggingY + (box.size.y - screenHeight) * alignY)
                        .coerceIn(-alignY * screenHeight, (-alignY + 1) * screenHeight) * factor).toInt().toDouble() /
                            factor + if (alignY == 0.5) 0.5 else 0.0


                if (!Mouse.isButtonDown(0)) dragging = false
            }
        }

        UIEngine.overlayContext.addChild(box)

        repeat(4) {
            box.children.add(
                rectangle {
                    size = V3(250.0, 16.0)
                    color = TRANSPARENT
                }
            )
        }

        UIEngine.registerHandler(PluginMessage::class.java) {
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

        val slotSize = 18.0

        fun reload(settings: Settings) {
            box.align.x = settings.alignX
            box.align.y = settings.alignY
            box.offset.x = settings.offsetX
            box.offset.y = settings.offsetY

            box.scale = V3(settings.scale, settings.scale, 1.0)

            box.size = V3(
                if (settings.vertical) slotSize + 10 else slotSize * 4 + 10,
                if (settings.vertical) slotSize * 4 + 10 else slotSize + 10,
            )

            for ((i, child) in box.children.withIndex()) {
                if (settings.vertical) child.offset = V3(120.0, i * slotSize + 5)
                else child.offset = V3(i * slotSize + 5, 5.0)
            }
        }

        reload(
            readSettings() ?: Settings(
                0.0, 0.0, 835.0, 430.0, 1.0, "", true
            )
        )
    }

    private fun saveSettings(settings: Settings) {
        try {
            Files.write(Paths.get("statistic.json"), gson.toJson(settings).toByteArray());
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    private fun readSettings(): Settings? {
        try {
            val readAllLines = Files.readAllLines(Paths.get("statistic.json"))
            if (readAllLines == null || readAllLines.isEmpty()) return null;
            return gson.fromJson(readAllLines.get(0), Settings::class.java)
        } catch (exception: Exception) {
            return null
        }
    }
}