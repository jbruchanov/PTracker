package com.scurab.ptracker.ext

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.nativeCanvas
import com.scurab.ptracker.ui.PriceBoardState

val DrawScope.nativeCanvas get() = drawContext.canvas.nativeCanvas

inline fun DrawScope.withTranslate(state: PriceBoardState, block: DrawScope.() -> Unit) {
    translate(state.offset.x, state.offset.y) {
        block()
    }
}

inline fun DrawScope.withTranslateAndScale(state: PriceBoardState, block: DrawScope.() -> Unit) {
    translate(state.offset.x, state.offset.y) {
        scale(state.scale.x, state.scale.y, pivot = state.chartScaleOffset()) {
            block()
        }
    }
}

inline fun DrawScope.resetScale(state: PriceBoardState, block: DrawScope.() -> Unit) {
    scale(scaleX = 1f / state.scale.x, scaleY = 1f / state.scale.y, pivot = Offset.Zero) {
        block()
    }
}