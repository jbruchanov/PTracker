package com.scurab.ptracker.ui.common

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import com.scurab.ptracker.ui.AppColors
import com.scurab.ptracker.ui.AppSizes
import com.scurab.ptracker.ui.AppTheme

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