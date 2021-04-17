package ru.func.mod

import ru.cristalix.uiengine.utility.V3
import java.util.*

data class ToSell(
    val address: String,
    val title: String,
    val min: V3,
    val max: V3,
    val cost: Double,
    val uuid: UUID
)
