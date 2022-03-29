package com.scurab.ptracker.ui.common

import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp

@Composable
fun Modifier.sizeAbsolute(widthPx: Float, heightPx: Float): Modifier {
    val density = LocalDensity.current.density
    return size(width = (widthPx / density).dp, height = (heightPx / density).dp)
}