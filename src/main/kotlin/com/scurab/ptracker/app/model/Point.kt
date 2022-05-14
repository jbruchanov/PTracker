package com.scurab.ptracker.app.model

import androidx.compose.ui.geometry.Size

interface IPoint {
    val x: Float
    val y: Float
}

data class Point(override val x: Float, override val y: Float) : IPoint {
    operator fun times(size: Size) = Point(x * size.width, y * size.height)
}

class MutablePoint() : IPoint {

    override var x: Float = 0f
    override var y: Float = 0f

    constructor(x: Float, y: Float) : this() {
        set(x, y)
    }

    fun set(x: Float, y: Float) {
        this.x = x
        this.y = y
    }
}