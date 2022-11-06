package com.scurab.ptracker.app.ext

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
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

fun List<MarketPercentage>.pieChartData2(groupingThreshold: Float = 0f): List<PieChartSegment> {
    val result = mutableListOf<PieChartSegment>()
    val angleOffset = -90
    val maxAngle = 360f + angleOffset
    var angle = 0f
    var sum = 0f
    var angle2 = 0f
    var sum2 = 0f
    val colors = map { it.asset }.colors()
    val coef = groupingThreshold * 100f
    val threshold = 1f / coef
    forEachIndexed { index, (_, perc) ->
        val groupRest = sum >= 1 - threshold
        val startAngle = angle + angleOffset
        val startAngle2 = if (groupRest) angle2 + angleOffset else 0f
        val sweep = if (isLastIndex(index)) maxAngle - startAngle else ((perc * 360f).roundToInt().toFloat())
        //show even 0% as at least 1deg sweep
        val sweep2 = ((if (groupRest) coef else 0f) * perc * 360f).roundToInt().coerceAtLeast(1).toFloat()
        val color = colors[index]
        result.add(PieChartSegment(startAngle, sweep, color, strokeWidth = 30.dp))
        if (groupRest) {
            result.add(PieChartSegment(startAngle2, sweep2.coerceAtMost(maxAngle), color, strokeWidth = 30.dp, radiusOffset = (-59).dp))
        }
        angle += sweep
        angle2 += sweep2
        sum += perc
        sum2 += if (groupRest) perc else 0f
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
