package com.scurab.ptracker.ext

import androidx.compose.ui.geometry.Size
import org.jetbrains.skia.TextLine

fun TextLine.size(padding: Float): Size = size(padding, padding, padding, padding)
fun TextLine.size(horizontalPadding: Float = 0f, verticalPadding: Float = 0f): Size = size(horizontalPadding, verticalPadding, horizontalPadding, verticalPadding)
fun TextLine.size(paddingLeft: Float = 0f, paddingTop: Float = 0f, paddingRight: Float = 0f, paddingBottom: Float = 0f): Size =
    Size(paddingLeft + width + paddingRight, paddingTop + height + paddingBottom)