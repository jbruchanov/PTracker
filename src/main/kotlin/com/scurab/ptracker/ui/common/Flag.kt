package com.scurab.ptracker.ui.common

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import com.jibru.kostra.compose.LocalQualifiers
import com.scurab.ptracker.K
import com.scurab.ptracker.app.ext.One
import com.scurab.ptracker.app.ext.nativeCanvas
import com.scurab.ptracker.app.ext.toFlagEmoji
import com.scurab.ptracker.get
import org.jetbrains.skia.Data
import org.jetbrains.skia.Font
import org.jetbrains.skia.Paint
import org.jetbrains.skia.TextLine
import org.jetbrains.skia.Typeface
import org.jetbrains.skia.shaper.ShapingOptions

@Composable
fun Flag(code: String, size: Dp, modifier: Modifier = Modifier) {
    val sizePx = size.value * LocalDensity.current.fontScale
    val qualifiers = LocalQualifiers.current
    val font = remember {
        K.root.babelstoneflags.get(qualifiers)
            .let { Font(Typeface.makeFromData(Data.makeFromBytes(it))) }
    }.also {
        it.size = sizePx
    }
    val paint = remember {
        Paint().also {
            it.isAntiAlias = true
            it.color = Color.White.toArgb()
        }
    }
    val textLineF = TextLine.make(code.toFlagEmoji(), font, ShapingOptions.DEFAULT)
    //clip offset around the flag glyph, values depend on babelstoneflags.ttf
    val l = 0.034f
    val t = 0.085f
    val r = 0.036f
    val b = 0.16f
    val start = Offset(x = textLineF.width * l, y = textLineF.height * t)
    val originalCanvasSize = Size((1 - r) * textLineF.width - start.x, (1 - b) * textLineF.height - start.y)
    //scaling doesn't work
    val isSquare = false
    val canvasSize = if (isSquare) Size(originalCanvasSize.minDimension, originalCanvasSize.minDimension) else originalCanvasSize
    Canvas(
        modifier = Modifier
            .sizeAbsolute(canvasSize.width, canvasSize.height, LocalDensity.current.density)
            .then(modifier)
    ) {
        val centerY = 0f + textLineF.height / 2 - (textLineF.descent + textLineF.ascent) / 2
        val offsetX = if (isSquare) (canvasSize.maxDimension - originalCanvasSize.width) / 2 else 0f
        val offset = Offset(x = -start.x + offsetX, y = -start.y + centerY)
        translate(left = 0f, top = centerY / 2) {
            translate(left = offset.x, top = offset.y) {
                val scale = if (isSquare) Offset(1.1f, 1.1f) else Offset.One
                val pivot = if (isSquare) -offset + this.center else Offset.Zero
                scale(scaleX = scale.x, scaleY = scale.y, pivot = pivot) {
                    nativeCanvas.drawTextLine(textLineF, 0f, 0f, paint)
                }
            }
        }
    }
}
