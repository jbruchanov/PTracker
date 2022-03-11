package com.scurab.ptracker.ext

import kotlin.math.abs
import kotlin.math.roundToInt

val Float.f0 get() = this.roundToInt().toString()
val Float.f3 get() = String.format("%.3f", this)

fun Float.priceRound(): Int {
    val abs = abs(this)
    val coef = when {
        abs > 10000f -> 1000f
        abs > 1000f -> 100f
        abs > 100f -> 10f
        abs > 10f -> 5f
        else -> 1f
    }
    return ((this / coef).roundToInt() * coef).roundToInt()
}