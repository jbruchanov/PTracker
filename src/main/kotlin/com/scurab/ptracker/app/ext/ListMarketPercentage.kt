package com.scurab.ptracker.app.ext

import androidx.compose.ui.graphics.Color
import com.scurab.ptracker.app.model.MarketPercentage
import com.scurab.ptracker.ui.common.PieChartSegment
import kotlin.math.roundToInt

fun List<MarketPercentage>.pieChartData(groupingThreshold: Float = 0f): List<PieChartSegment> {
    val result = mutableListOf<PieChartSegment>()

    var angle = -90f
    val maxAngle = 360 + angle
    var sum = 0f
    val colors = map { it.asset }.colors()
    forEachGrouping(groupingThreshold) { index, (_, perc), groupRest ->
        val sweep = ((if (groupRest) 1f - sum else perc) * 360f).roundToInt().toFloat()
        val color = if (groupRest) Color.White else colors[index]
        result.add(PieChartSegment(angle, sweep.coerceAtMost(maxAngle), color))
        angle += sweep
        sum += perc
        if (groupRest) {
            return result
        }
    }
    return result
}

inline fun List<MarketPercentage>.forEachGrouping(groupingThreshold: Float = 0f, block: (Int, MarketPercentage, Boolean) -> Unit) {
    var sum = 0f
    forEachIndexed { index, item ->
        val groupRest = item.percentage <= groupingThreshold
        block(index, item, groupRest)
        sum += item.percentage
    }
}