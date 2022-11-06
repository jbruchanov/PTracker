package com.scurab.ptracker.ui.common

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.text.style.TextAlign
import com.scurab.ptracker.app.ext.nativeCanvas
import com.scurab.ptracker.ui.AppTheme
import org.jetbrains.skia.TextLine

/**
 * Very simple & fast text drawing
 */
@Composable
fun FastText(value: String, modifier: Modifier = Modifier, textAlign: TextAlign = TextAlign.Center) {
    val line = remember(value) { TextLine.make(value, AppTheme.TextRendering.fontTableText) }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clipToBounds()
            .then(modifier)
            .drawWithContent {
                val canvasSize = this@drawWithContent.size
                val x = when (textAlign) {
                    TextAlign.Center -> (canvasSize.width - line.width) / 2f
                    TextAlign.End -> (canvasSize.width - line.width)
                    else -> 0f
                }
                val y = when (textAlign) {
                    TextAlign.Center -> (canvasSize.height - line.height) / 2f
                    TextAlign.End -> (canvasSize.height - line.height)
                    else -> 0f
                }
                nativeCanvas.drawTextLine(line, x, -line.ascent + y, AppTheme.TextRendering.paint)
            }
    )
}
