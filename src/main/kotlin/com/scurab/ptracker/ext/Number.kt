package com.scurab.ptracker.ext

import com.scurab.ptracker.model.FiatCurrencies
import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.math.ceil
import kotlin.math.log10
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


val Int.bd get() = toBigDecimal()
val Double.bd get() = toBigDecimal()

fun BigDecimal.isZero() = compareTo(BigDecimal.ZERO) == 0
fun BigDecimal.base() = ceil(log10(toDouble())).toInt()

fun BigDecimal.round(asset: String?, scaleFiat: Int = 4, scaleCrypto: Int = 8): BigDecimal {
    return if (asset != null && FiatCurrencies.contains(asset) && scale() > scaleFiat) {
        setScale(scaleFiat, RoundingMode.HALF_UP)
    } else if (scale() > scaleCrypto) {
        setScale(scaleCrypto, RoundingMode.HALF_UP)
    } else this
}