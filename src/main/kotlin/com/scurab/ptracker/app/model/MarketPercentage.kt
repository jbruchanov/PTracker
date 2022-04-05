package com.scurab.ptracker.app.model

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import com.scurab.ptracker.ui.AppTheme
import com.scurab.ptracker.ui.common.PieChartSegment
import kotlin.math.roundToInt

data class MarketPercentage(val asset: Asset, val percentage: Float)

fun Collection<MarketPercentage>.pieChartData(): List<PieChartSegment> {
    val result = mutableListOf<PieChartSegment>()
    val colors = listOf(Color.Red, Color.Green, Color.Yellow, Color.LightGray, Color.Cyan)
        .map {
            it.copy(0.5f)
                .compositeOver(AppTheme.Colors.Primary)
        }

    var angle = 0f
    forEachIndexed { index, (asset, perc) ->
        val sweep = (perc * 360f).roundToInt().toFloat()
        result.add(PieChartSegment(angle, sweep.coerceAtMost(360f - angle), colors[index % colors.size]))
        angle += sweep
    }
    return result
}