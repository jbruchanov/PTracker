package com.scurab.ptracker.app.model

class LineChartData(
    val stats: List<GroupStatsSum>,
    val marketPrice: List<Point>,
    val cost: List<Point>,
    val latestMarketPrice: List<Point>,
    val maxMarketPrice: Point,
    val minMarketPriceXSinceMax: Point?
) {
    //y flipped for canvas
    val hasProfit = (marketPrice.lastOrNull()?.y ?: 0f) < (cost.lastOrNull()?.y ?: 0f)

    companion object {
        val Empty = LineChartData(emptyList(), emptyList(), emptyList(), emptyList(), IPoint.Empty, IPoint.Empty)
    }
}