package com.scurab.ptracker.app.ext

import com.scurab.ptracker.app.model.FiatCurrencies
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import kotlin.math.ceil
import kotlin.math.log10

val ZERO = "0".bd

fun BigDecimal.isZero() = compareTo(ZERO) == 0
fun BigDecimal.isNotZero() = !isZero()
fun BigDecimal.base() = ceil(log10(toDouble())).toInt()
fun BigDecimal.inverse() = (1.bd / this).align
fun BigDecimal.round(asset: String?, scaleFiat: Int = 4, scaleCrypto: Int = 8): BigDecimal =
    round(asset != null && FiatCurrencies.contains(asset), scaleFiat, scaleCrypto)

fun BigDecimal.round(isFiat: Boolean, scaleFiat: Int = 4, scaleCrypto: Int = 8): BigDecimal {
    return if (isFiat && scale() > scaleFiat) {
        setScale(scaleFiat, RoundingMode.HALF_UP)
    } else if (scale() > scaleCrypto) {
        setScale(scaleCrypto, RoundingMode.HALF_UP)
    } else this
}

fun BigDecimal.valueIf(value: Boolean, falseValue: BigDecimal = ZERO) = if (value) this else falseValue

val BigDecimal.f8: String get() = f(8)
val BigDecimal.f6 get() = f(6)
val BigDecimal.f4: String get() = f(4)
val BigDecimal.gf4: String get() = gf(4)
val BigDecimal.f2 get() = f(2)
val BigDecimal.percf2 get() = (this * 100.bd).f(2) + "%"
val BigDecimal.gf2 get() = gf(2)
fun BigDecimal.f(digits: Int): String = BigDecimalFormats.formats[digits].value.format(this)
fun BigDecimal.gf(digits: Int): String = BigDecimalFormats.groupingFormats[digits].value.format(this)

val BigDecimal.align: BigDecimal get() = align(8)
fun BigDecimal.align(scale: Int) = setScale(scale, RoundingMode.HALF_UP)

val BigDecimal.isPositive get() = this > ZERO
val BigDecimal.isNegative get() = this < ZERO
fun BigDecimal.safeDiv(divisor: BigDecimal): BigDecimal = if (divisor.isZero()) ZERO else this.align / divisor
fun BigDecimal.roi() = takeIf { !it.isZero() }
    ?.let { v -> (if (v > 1.bd) v - 1.bd else -(1.bd - v)) * 100.bd }
    ?: ZERO

object BigDecimalFormats {
    val formats = (0..8).map {
        lazy {
            DecimalFormat().apply {
                maximumFractionDigits = it
                minimumFractionDigits = it
                isGroupingUsed = false
                roundingMode = RoundingMode.HALF_UP
            }
        }
    }
    val groupingFormats = (0..8).map {
        lazy {
            DecimalFormat().apply {
                maximumFractionDigits = it
                minimumFractionDigits = it
                roundingMode = RoundingMode.HALF_UP
                isGroupingUsed = true
            }
        }
    }
    val justGrouping get() = groupingFormats[8].value
}

