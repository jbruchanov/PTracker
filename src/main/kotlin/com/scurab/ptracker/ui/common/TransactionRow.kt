package com.scurab.ptracker.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.scurab.ptracker.ext.formattedPrices
import com.scurab.ptracker.ext.iconColor
import com.scurab.ptracker.model.Transaction
import com.scurab.ptracker.ui.AppColors
import com.scurab.ptracker.ui.AppSizes
import com.scurab.ptracker.ui.AppTheme
import com.scurab.ptracker.ui.DateTimeFormats
import org.koin.java.KoinJavaComponent

@Composable
fun TransactionRow(index: Int, item: Transaction, isSelected: Boolean) {
    val formatter = remember { KoinJavaComponent.getKoin().get<DateTimeFormats>() }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = AppSizes.current.Space)
            .background(AppColors.current.RowBackground.get(isSelected = isSelected, isEven = index % 2 == 0))
            .padding(vertical = AppSizes.current.Space, horizontal = AppSizes.current.Space2)
    ) {
        val prices = item.formattedPrices()
        Column {
            Row {
                Text(text = formatter.fullDateTime(item.dateTime), style = AppTheme.TextStyles.TransactionSecondary)
                Spacer(modifier = Modifier.weight(1f))
                val iconColor = item.iconColor()
                val size = 16f
                Icon(
                    iconColor.image,
                    contentDescription = "",
                    tint = iconColor.color,
                    modifier = Modifier
                        .size(size.dp)
                        .offset(iconColor.offset.x, -iconColor.offset.y * (size / iconColor.image.defaultHeight.value))
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
            Spacer(modifier = Modifier.height(AppSizes.current.Space))
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