package com.scurab.ptracker.app.ext

import androidx.compose.ui.unit.Density
import java.lang.Float.max

fun Density.maxValue() : Float = max(density, fontScale)