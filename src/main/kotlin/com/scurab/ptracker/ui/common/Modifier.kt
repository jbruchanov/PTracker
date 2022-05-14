package com.scurab.ptracker.ui.common

import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp

@Composable
fun Modifier.sizeAbsolute(widthPx: Float, heightPx: Float, density: Density = LocalDensity.current) =
    sizeAbsolute(widthPx, heightPx, density.density)

fun Modifier.sizeAbsolute(widthPx: Float, heightPx: Float, density: Float): Modifier {
    return size(width = (widthPx / density).dp, height = (heightPx / density).dp)
}