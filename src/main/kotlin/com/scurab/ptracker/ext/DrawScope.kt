package com.scurab.ptracker.ext

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.vector.VectorPainter
import com.scurab.ptracker.ui.priceboard.PriceBoardState
import java.lang.Float.max

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
inline fun DrawScope.withTranslateAndScale(state: PriceBoardState, block: DrawScope.() -> Unit) {
    //move the origin to bottomLeft corner
    translate(-state.offset.x, state.offset.y + state.canvasSize.height) {
        //scale with pivot at the right, height/2
        scale(state.scale.x, state.scale.y, pivot = state.chartScalePivot()) {
            //flip y to grow up
            scale(1f, -1f, pivot = Offset.Zero) {
                block()
            }
        }
    }
}

inline fun DrawScope.resetScale(state: PriceBoardState, block: DrawScope.() -> Unit) {
    scale(scaleX = 1f / state.scale.x, scaleY = -1f / state.scale.y, pivot = Offset.Zero) {
        block()
    }
}

//due to internal check for clip, right/bottom must be >= 0
inline fun DrawScope.clipRectSafe(
    left: Float = 0.0f,
    top: Float = 0.0f,
    right: Float = size.width,
    bottom: Float = size.height,
    clipOp: ClipOp = ClipOp.Intersect,
    block: DrawScope.() -> Unit
) = withTransform({ clipRect(left, top, max(0f, right), max(0f, bottom), clipOp) }, block)


fun DrawScope.draw(vectorPainter: VectorPainter, scale: Offset, colorFilter: ColorFilter? = null) {
    val size = vectorPainter.intrinsicSize * scale
    translate(-size.width / 2, -size.height / 2) {
        with(vectorPainter) { draw(size, colorFilter = colorFilter) }
    }
}