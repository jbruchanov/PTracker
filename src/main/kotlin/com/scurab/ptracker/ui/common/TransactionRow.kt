@file:OptIn(ExperimentalFoundationApi::class)

package com.scurab.ptracker.ui.common

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.HoverInteraction
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import com.scurab.ptracker.app.ext.formattedPrices
import com.scurab.ptracker.app.ext.iconColor
import com.scurab.ptracker.app.ext.scaled
import com.scurab.ptracker.app.model.Transaction
import com.scurab.ptracker.ui.AppColors
import com.scurab.ptracker.ui.AppSizes
import com.scurab.ptracker.ui.AppTheme
import com.scurab.ptracker.ui.DateTimeFormats
import org.koin.java.KoinJavaComponent

@Composable
fun TransactionRow(
    onClick: (Boolean) -> Unit,
    onHoverChange: (Boolean) -> Unit,
    index: Int,
    item: Transaction,
    isSelected: Boolean,
    modifier: Modifier = Modifier
) {
    val formatter = remember { KoinJavaComponent.getKoin().get<DateTimeFormats>() }
    val hoverableInteractionSource = remember { MutableInteractionSource() }
    LaunchedEffect(item) {
        hoverableInteractionSource.interactions.collect {
            onHoverChange(it is HoverInteraction.Enter)
        }
    }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = AppSizes.current.Space)
            .background(AppColors.current.RowBackground.get(isSelected = isSelected, isEven = index % 2 == 0))
            .combinedClickable(
                onClick = { onClick(false) },
                onDoubleClick = { onClick(true) }
            )
            .hoverable(hoverableInteractionSource)
            .padding(vertical = AppSizes.current.Space, horizontal = AppSizes.current.Space2)
    ) {
        val prices = item.formattedPrices()
        Column {
            Row {
                Text(text = formatter.fullDateTime(item.dateTime), style = AppTheme.TextStyles.TransactionSecondary)
                FSpacer()
                val iconColor = item.iconColor()
                val size = AppSizes.current.IconTransactionType.scaled()
                Icon(
                    iconColor.imageVector.get(),
                    contentDescription = "",
                    tint = iconColor.color.default,
                    modifier = Modifier.size(size)
                )
            }
            if (prices.buy != null) {
                Row {
                    Text(text = prices.buy, style = AppTheme.TextStyles.TransactionPrimary)
                    AnnotatedText(text = item.typeIfNotTradeElseText("Buy"))
                }
            }
            if (prices.sell != null) {
                Row {
                    Text(text = prices.sell, style = AppTheme.TextStyles.TransactionPrimary)
                    AnnotatedText(text = item.typeIfNotTradeElseText("Sell"))
                }
            }
            if (prices.fee != null) {
                Row {
                    Text(text = prices.fee, style = AppTheme.TextStyles.TransactionPrimary)
                    AnnotatedText(text = "Fee")
                }
            }
            if (prices.unitPrice != null) {
                Row {
                    Text(text = prices.unitPrice, style = AppTheme.TextStyles.TransactionPrimaryVariant)
                    AnnotatedText(text = "Price")
                }
            }
            HSpacer()
            Text(text = item.exchange, style = AppTheme.TextStyles.TransactionDetail)
        }
    }
}

private fun Transaction.typeIfNotTradeElseText(text: String) = if (this is Transaction.Trade) text else type

@Composable
private fun RowScope.AnnotatedText(text: String) {
    Text(
        text = text,
        style = AppTheme.TextStyles.TransactionMoneyAnnotation,
        modifier = Modifier.weight(1f).align(Alignment.CenterVertically),
        textAlign = TextAlign.End
    )
}