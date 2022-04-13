package com.scurab.ptracker.app.model

class LineChartData(
    val marketPrice: List<Point>, val cost: List<Point>
) {
    //y flipped for canvas
    val hasProfit = (marketPrice.lastOrNull()?.y ?: 0f) < (cost.lastOrNull()?.y ?: 0f)
}