package com.scurab.ptracker.ext

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.nativeCanvas
import com.scurab.ptracker.ui.PriceBoardState
import com.scurab.ptracker.ui.chartScalePivot

val DrawScope.nativeCanvas get() = drawContext.canvas.nativeCanvas

inline fun DrawScope.withTranslate(state: PriceBoardState, block: DrawScope.() -> Unit) {
    translate(state.offset.x, state.offset.y) {
        block()
    }
}


/*
inline fun DrawScope.withTranslateAndScale(state: PriceBoardState, block: DrawScope.() -> Unit) {
    //move the origion to bottomLeft corner
    translate(-state.offset.x, state.offset.y + state.canvasSize.height) {
        //scale with pivot at the right, height/2
        scale(state.scale.x, state.scale.y, pivot = Offset(state.offset.x + state.verticalPriceBarLeft(state.localDensity.density), -state.offset.y - size.height / 2)) {
            //flip y to grow up
            scale(1f, -1f, pivot = Offset.Zero) {
                block()
            }
        }
    }
}
*/

/**
 * translate and scale to have [0,0] at bottom left corner and [+x, +y] with top/right corner
 */
inline fun DrawScope.withTranslateAndScale(
    state: PriceBoardState,
    offsetX: Float = -state.offset.x,
    offsetY: Float = state.offset.y + state.canvasSize.height,
    scaleX: Float = state.scale.x,
    scaleY: Float = state.scale.y,
    scalePivot: Offset = state.chartScalePivot(),
    flipY: Boolean = true,
    block: DrawScope .() -> Unit
) {
//    translate(-state.offset.x, state.offset.y + state.canvasSize.height) {
//        scale(state.scale.x, state.scale.y, pivot = state.chartScaleOffset()) {
//            scale(1f, -1f, pivot = Offset.Zero) {
//                block()
//            }
//        }
//    }
    //move the origion to bottomLeft corner
    translate(offsetX, offsetY) {
        //scale with pivot at the right, height/2
        scale(scaleX, scaleY, pivot = scalePivot) {
            //flip y to grow up
            scale(1f, (!flipY).sign(), pivot = Offset.Zero) {
                block()
            }
        }
    }
}

inline fun DrawScope.resetScale(state: PriceBoardState, block: DrawScope.() -> Unit) {
    scale(scaleX = 1f / state.scale.x, scaleY = 1f / state.scale.y, pivot = Offset.Zero) {
        block()
    }
}