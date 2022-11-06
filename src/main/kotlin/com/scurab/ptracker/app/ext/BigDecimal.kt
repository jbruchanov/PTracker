package com.scurab.ptracker.app.ext

import com.scurab.ptracker.app.model.FiatCurrencies
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract
import kotlin.math.ceil
import kotlin.math.log10

val ZERO = "0".bd

fun BigDecimal.isZero() = compareTo(ZERO) == 0
fun BigDecimal.isNotZero() = !isZero()
@OptIn(ExperimentalContracts::class)
fun BigDecimal?.isNotNullAndNotZero(): Boolean {
    contract {
        returns() implies (this@isNotNullAndNotZero != null)
    }
    return this != null && !isZero()
}
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
val BigDecimal.percf2 get() = (this * 100.bd).f(2) + "%"
val BigDecimal.s2 get() = (this.setScale(2, RoundingMode.HALF_UP))
fun BigDecimal.f(digits: Int): String = BigDecimalFormats.formats[digits].value.format(this)
fun BigDecimal.gf(digits: Int): String = BigDecimalFormats.groupingFormats[digits].value.format(this)

fun BigDecimal.hr(): BigDecimal {
    repeat(4) {
        val scale = (it * 2) + 2
        val v = setScale(scale, RoundingMode.HALF_UP)
        val scalePlus1 = (it != 0 || v.toInt() < 10)
        if (!v.isZero()) return setScale(scale + scalePlus1.int(), RoundingMode.HALF_UP)
    }
    return ZERO.setScale(2)
}

fun BigDecimal.hrs(): String = hr().toPlainString()
val BigDecimal.align: BigDecimal get() = align(8)
fun BigDecimal.align(scale: Int) = setScale(scale, RoundingMode.HALF_UP)

val BigDecimal.isPositive get() = this > ZERO
val BigDecimal.isNegative get() = this < ZERO
val BigDecimal.isZeroOrPositive get() = this >= ZERO
fun BigDecimal.safeDiv(divisor: BigDecimal): BigDecimal = (if (divisor.isZero()) ZERO else this.align / divisor).align
fun BigDecimal.roi() = takeIf { !it.isZero() }
    ?.let { v -> (if (v > 1.bd) v - 1.bd else -(1.bd - v)) * 100.bd }
    ?: ZERO

fun BigDecimal.toTableString(scale: Int = 6, minTrailingZeros: Int = 2) = setScale(scale, RoundingMode.HALF_UP)
    .toPlainString()
    .let { str ->
        //replace ending "0" by " "
        var addSpaces = true
        val sb = StringBuilder()
        sb.setLength(str.length)
        (str.length - 1 downTo 0).forEach { i ->
            val c = str[i]
            val preIsDot = str.getOrNull(i - minTrailingZeros) == '.'
            addSpaces = addSpaces && !preIsDot
            sb[i] = if (addSpaces && c == '0') ' ' else c
            addSpaces = addSpaces && c == '0'
        }
        sb.toString()
    }

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
