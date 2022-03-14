package com.scurab.ptracker.ext

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import java.lang.Float.max

fun Dp.toPx(density: Float): Float = value * density

@Composable
fun Dp.toPx(provider: ProvidableCompositionLocal<Density>): Float = toPx(provider.current.density)

@Composable
fun TextUnit.toPx(provider: ProvidableCompositionLocal<Density>): Float {
    require(isSp) { "This is not Sp, it's $type" }
    return value * provider.current.maxValue()
}