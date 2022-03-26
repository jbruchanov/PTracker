package com.scurab.ptracker.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.scurab.ptracker.ui.AppColors
import com.scurab.ptracker.ui.AppSizes
import com.scurab.ptracker.ui.AppTheme

@Composable
fun ToggleButton(
    text: String, isSelected: Boolean, onClick: () -> Unit
) {
    val background = AppColors.current.ToggleButtonBackground.get(isSelected = isSelected)
    val textColor = AppColors.current.Content.get(isSelected = isSelected)
    Box(
        modifier = Modifier
            .background(background)
            .clickable(onClick = onClick)
            .padding(horizontal = AppSizes.current.Space4)
            .defaultMinSize(minWidth = AppSizes.current.MinClickableSize, minHeight = AppSizes.current.MinClickableSize)

    ) {
        Text(
            text = text, fontSize = AppTheme.TextRendering.small, color = textColor, modifier = Modifier.align(Alignment.Center)
        )
    }
}