package com.scurab.ptracker.ui.model

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import com.scurab.ptracker.app.ext.hrs
import com.scurab.ptracker.app.ext.isNotZero
import com.scurab.ptracker.app.ext.isPositive
import com.scurab.ptracker.app.ext.isZero
import com.scurab.ptracker.component.compose.StateContainer
import java.math.BigDecimal

data class PriceBoardAveragePrices(
    val avgMarketPrice: BigDecimal,
    val avgBuyPrice: BigDecimal
) {
    val isEmpty = avgMarketPrice.isZero() && avgBuyPrice.isZero()
    fun label(market: Color, trade: Color, tradingColor: StateContainer<Color>): AnnotatedString {
        return AnnotatedString.Builder().apply {
            if (avgMarketPrice.isNotZero()) {
                append("M")
                append(AnnotatedString(avgMarketPrice.hrs(), SpanStyle(color = market)))
            }
            if (avgBuyPrice.isNotZero()) {
                if (!isEmpty) append(" ")
                append("B")
                append(AnnotatedString(avgBuyPrice.hrs(), SpanStyle(color = trade)))
            }
            if (avgMarketPrice.isNotZero() && avgBuyPrice.isNotZero()) {
                if (!isEmpty) append(" ")
                append("D")
                val diff = avgMarketPrice - avgBuyPrice
                val color = tradingColor.default2If(diff.isPositive)
                append(AnnotatedString(diff.hrs(), SpanStyle(color = color)))
            }

        }.toAnnotatedString()
    }
}