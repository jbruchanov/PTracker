package com.scurab.ptracker.app.ext

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import java.math.BigDecimal

val String.bd: BigDecimal get() = BigDecimal(this, DefaultMathContext)

fun String.toFlagEmoji(): String {
    require(this.length >= 2) { "Invalid code:${this}, needs at least 2 chars" }
    val flagOffset = 0x1F1E6
    val asciiOffset = 0x41
    val firstChar = Character.codePointAt(this, 0) - asciiOffset + flagOffset
    val secondChar = Character.codePointAt(this, 1) - asciiOffset + flagOffset
    return (String(Character.toChars(firstChar)) + String(Character.toChars(secondChar)))
}


fun String.colored(color: Color) = AnnotatedString(
    text = this,
    spanStyle = SpanStyle(color = color)
)