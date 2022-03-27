package com.scurab.ptracker.ui.model

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.DpOffset
import com.scurab.ptracker.ui.AppTheme


class IconColor(
    val drawPriority: Int,
    val image: ImageVector,
    val color: Color,
    val scale: Offset = AppTheme.DashboardSizes.TransctionIconScale,
    val offset: DpOffset = DpOffset.Zero
)