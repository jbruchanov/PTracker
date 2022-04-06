package com.scurab.ptracker.app.ext

import kotlin.math.roundToInt

val Float.f0 get() = this.roundToInt().toString()
val Float.f3 get() = this.f(3)
val Float.f2 get() = this.f(2)
fun Float.f(decimals: Int) = String.format("%,.${decimals}f", this)

private val FloatRange.minMaxDiff get() = endInclusive - start

fun Float.toLabelPrice(visiblePriceRange: FloatRange): String {
    if (this == 0f) return "0"
    val minMaxDiff = visiblePriceRange.minMaxDiff
    val decimalPlaces = visiblePriceRange.getLabelPriceDecimals()
    return when {
        minMaxDiff > 1e6 -> round(1000f, decimalPlaces)
        minMaxDiff > 20000 -> round(500f, decimalPlaces)
        minMaxDiff > 5000 -> round(100f, decimalPlaces)
        minMaxDiff > 1000 -> round(50f, decimalPlaces)
        minMaxDiff > 100 -> round(5f, decimalPlaces)
        else -> this.f(decimalPlaces)
    }
}

fun FloatRange.getLabelPriceDecimals(): Int {
    return when {
        minMaxDiff > 100 -> 2
        minMaxDiff > 0.1 -> 3
        minMaxDiff > 0.01 -> 4
        minMaxDiff > 0.001 -> 5
        else -> 6
    }
}

private fun Float.round(base: Float, decimals: Int = 2) = ((this / base).roundToInt() * base).roundToInt().toFloat().f(decimals)

val Int.bd get() = toBigDecimal().align
val Double.bd get() = toBigDecimal().align
