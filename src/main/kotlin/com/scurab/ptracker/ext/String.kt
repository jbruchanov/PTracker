package com.scurab.ptracker.ext

import java.math.BigDecimal

val String.bd: BigDecimal get() = BigDecimal(this).align

fun String.toFlagEmoji(): String {
    require(this.length >= 2) { "Invalid code:${this}, needs at least 2 chars" }
    val flagOffset = 0x1F1E6
    val asciiOffset = 0x41
    val firstChar = Character.codePointAt(this, 0) - asciiOffset + flagOffset
    val secondChar = Character.codePointAt(this, 1) - asciiOffset + flagOffset
    return (String(Character.toChars(firstChar)) + String(Character.toChars(secondChar)))
}