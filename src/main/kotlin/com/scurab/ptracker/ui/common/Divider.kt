package com.scurab.ptracker.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.scurab.ptracker.ui.AppTheme

@Composable
fun VerticalDivider(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colors.onSurface.copy(alpha = AppTheme.Values.DividerDefaultAlpha),
    thickness: Dp = AppTheme.Sizes.Hairline,
    startIndent: Dp = 0.dp
) {
    val indentMod = if (startIndent.value != 0f) Modifier.padding(start = startIndent) else Modifier
    val targetThickness = if (thickness == Dp.Hairline) (1f / LocalDensity.current.density).dp else thickness
    Box(
        modifier.then(indentMod)
            .fillMaxHeight()
            .width(targetThickness)
            .background(color = color)
    )
}

@Composable
fun Divider(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colors.onSurface.copy(alpha = AppTheme.Values.DividerDefaultAlpha),
    thickness: Dp = AppTheme.Sizes.Hairline,
    startIndent: Dp = 0.dp
) {
    val indentMod = if (startIndent.value != 0f) Modifier.padding(start = startIndent) else Modifier
    val targetThickness = if (thickness == Dp.Hairline) (1f / LocalDensity.current.density).dp else thickness
    Box(
        modifier.then(indentMod)
            .fillMaxWidth()
            .height(targetThickness)
            .background(color = color)
    )
}