package com.scurab.ptracker.app.ext

import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat

private val ZERO = "0".bd
fun BigDecimal.valueIf(value: Boolean, falseValue: BigDecimal = ZERO) = if (value) this else falseValue

val BigDecimal.f8: String get() = f(8)
val BigDecimal.f6 get() = f(6)
val BigDecimal.f4: String get() = f(4)
val BigDecimal.gf4: String get() = gf(4)
val BigDecimal.f2 get() = f(2)
val BigDecimal.gf2 get() = gf(2)
fun BigDecimal.f(digits: Int): String = BigDecimalFormats.formats[digits].value.format(this)
fun BigDecimal.gf(digits: Int): String = BigDecimalFormats.groupingFormats[digits].value.format(this)

val BigDecimal.align: BigDecimal get() = align(8)
fun BigDecimal.align(scale: Int) = setScale(scale, RoundingMode.HALF_UP)

val BigDecimal.isPositive get() = this > ZERO
val BigDecimal.isNegative get() = this < ZERO
fun BigDecimal.safeDiv(divisor: BigDecimal): BigDecimal = if (divisor.isZero()) BigDecimal.ZERO else this / divisor
fun BigDecimal.roi() = takeIf { !it.isZero() }
    ?.let { v -> (if (v > 1.bd) v - 1.bd else -(1.bd - v)) * 100.bd }
    ?: BigDecimal.ZERO

object BigDecimalFormats {
    val formats = (0..8).map {
        lazy {
            DecimalFormat().apply {
                maximumFractionDigits = it
                minimumFractionDigits = it
                isGroupingUsed = false
            }
        }
    }
    val groupingFormats = (0..8).map {
        lazy {
            DecimalFormat().apply {
                maximumFractionDigits = it
                minimumFractionDigits = it
                isGroupingUsed = true
            }
        }
    }
}

