package com.scurab.ptracker.ui.model

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.scurab.ptracker.component.compose.StateContainer

class IconColor(
    val priority: Int = 0,
    val imageVector: StateContainer<ImageVector>,
    val color: StateContainer<Color>,
    val candleOffset: Offset = Offset.Zero,
    val candleScale: StateContainer<Offset> = CandleScale
) {
    constructor(
        priority: Int = 0,
        imageVector: ImageVector,
        color: Color,
        candleOffset: Offset = Offset.Zero,
        candleScale: StateContainer<Offset> = CandleScale
    ) : this(priority, StateContainer(imageVector), StateContainer(color), candleOffset, candleScale)

    companion object {
        val CandleScale = StateContainer(Offset(0.6f, 0.6f), selected = Offset(1.2f, 1.2f))
        val CandleScaleTrade = StateContainer(Offset(0.5f, 0.5f), selected = Offset(1f, 1f))
        val CandleScaleGrouping = StateContainer(Offset(0.5f, 0.5f), selected = Offset(0.5f, 0.5f))
    }
}