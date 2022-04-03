package com.scurab.ptracker.app.ext

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit

fun Dp.toPx(density: Float): Float = value * density

@Composable
fun Dp.toPx(provider: ProvidableCompositionLocal<Density>): Float = toPx(provider.current.density)

@Composable
fun TextUnit.toPx(provider: ProvidableCompositionLocal<Density>): Float {
    require(isSp) { "This is not Sp, it's $type" }
    return value * provider.current.maxValue()
}

@Composable
fun Dp.scaled(scale: Float = LocalDensity.current.maxValue()) = this * scale