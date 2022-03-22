package com.scurab.ptracker.ext

import kotlin.math.abs
import kotlin.math.roundToInt

val Float.f0 get() = this.roundToInt().toString()
val Float.f3 get() = String.format("%.3f", this)

fun Float.priceRound(minMaxDiff: Float): Float {
    val abs = abs(this)
    val coef = when {
        abs > 10000 && minMaxDiff > 20000 -> 500f
        abs > 1000 && minMaxDiff > 5000 -> 100f
        else -> 10f
    }
    return ((this / coef).roundToInt() * coef).roundToInt().toFloat()
}
val Int.bd get() = toBigDecimal()
val Double.bd get() = toBigDecimal()