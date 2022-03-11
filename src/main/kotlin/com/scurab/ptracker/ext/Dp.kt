package com.scurab.ptracker.ext

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp

fun Dp.toPx(density: Float): Float = value * density

@Composable
fun Dp.toPx(provider: ProvidableCompositionLocal<Density>): Float = toPx(provider.current.density)