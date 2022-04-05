package com.scurab.ptracker.app.ext

import androidx.compose.ui.graphics.Color
import com.scurab.ptracker.app.model.Asset
import com.scurab.ptracker.ui.CoinColors
import com.scurab.ptracker.ui.RandomColors

fun List<Asset>.colors(): List<Color> {
    var colorIndex = 0
    return map { CoinColors[it.coin1] ?: CoinColors[it.coin2] ?: RandomColors[colorIndex++ % size] }
}