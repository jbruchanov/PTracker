package com.scurab.ptracker.ext

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import com.scurab.ptracker.ui.PriceBoardState

inline fun DrawScope.withTranslate(state: PriceBoardState, block: DrawScope.() -> Unit) {
    translate(state.offsetX, state.offsetY) {
        block()
    }
}

inline fun DrawScope.withTranslateAndScale(state: PriceBoardState, block: DrawScope.() -> Unit) {
    translate(state.offsetX, state.offsetY) {
        scale(state.scaleX, state.scaleY, pivot = state.chartScaleOffset(size)) {
            block()
        }
    }
}

inline fun DrawScope.resetScale(state: PriceBoardState, block: DrawScope.() -> Unit) {
    scale(scaleX = 1f / state.scaleX, scaleY = 1f / state.scaleY, pivot = Offset.Zero) {
        block()
    }
}