package com.scurab.ptracker.ui.common

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.scurab.ptracker.ui.AppColors
import com.scurab.ptracker.ui.AppSizes
import com.scurab.ptracker.ui.AppTheme
import com.scurab.ptracker.ui.model.AssetIcon

@Composable
fun ToggleButton(
    text: String, isSelected: Boolean, onClick: () -> Unit
) {
    val textColor = AppColors.current.Content.get(isSelected = isSelected)
    ToggleButton(isSelected, onClick = onClick) {
        Text(
            text = text, fontSize = AppTheme.TextRendering.small, color = textColor, modifier = Modifier
                .padding(horizontal = AppSizes.current.Space4)
                .align(Alignment.Center)
        )
    }
}

@Composable
fun ToggleButton(
    imageVector: ImageVector, isSelected: Boolean, onClick: () -> Unit
) {
    val tint = AppColors.current.Content.get(isSelected = isSelected)
    val background = AppColors.current.ButtonBackground.get(isSelected = isSelected)
    ToggleButton(isSelected, onClick = onClick) {
        Image(
            imageVector,
            contentDescription = "",
            colorFilter = ColorFilter.tint(color = tint),
            modifier = Modifier
                .align(Alignment.Center)
                .defaultMinSize(minWidth = AppSizes.current.ClickableSize, minHeight = AppSizes.current.MinClickableSize)
                .padding(AppSizes.current.IconButtonPadding)
                .background(background)
        )
    }
}

@Composable
fun AssetToggleButton(
    item: AssetIcon, isSelected: Boolean, onClick: () -> Unit
) {
    val color = AppColors.current.Content.get(isSelected = isSelected)
    val background = AppColors.current.ButtonBackground.get(isSelected = isSelected)
    ToggleButton(isSelected, onClick = onClick) {
        Row {
            //unfinished
            Box(modifier = Modifier.background(Color.Black).padding(4.dp)) {
                Flag(code = item.asset.fiat, size = 12.sp, modifier = Modifier)
                if (item.icon != null) {
                    Image(item.icon, contentDescription = item.asset.crypto, modifier = Modifier.size(24.dp))
                }
            }
            Column(
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .wrapContentHeight(align = Alignment.CenterVertically)
            ) {
                Text(text = item.asset.crypto, fontSize = AppTheme.TextRendering.small, color = color, modifier = Modifier)
                Text(text = item.asset.fiat, fontSize = AppTheme.TextRendering.small, color = color, modifier = Modifier)
            }
        }
    }
}

@Composable
private fun ToggleButton(isSelected: Boolean, onClick: () -> Unit, content: @Composable BoxScope.() -> Unit) {
    val background = AppColors.current.ButtonBackground.get(isSelected = isSelected)
    Box(
        modifier = Modifier
            .background(background)
            .clickable(onClick = onClick)
            .defaultMinSize(minWidth = AppSizes.current.MinClickableSize, minHeight = AppSizes.current.MinClickableSize)
    ) {
        content()
    }
}