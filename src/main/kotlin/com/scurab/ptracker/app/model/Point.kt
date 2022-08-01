package com.scurab.ptracker.app.model

import androidx.compose.ui.geometry.Size

interface IPoint {
    val x: Float
    val y: Float

    companion object {
        val Empty = Point(Float.NaN, Float.NaN)
    }
}

data class Point(override val x: Float, override val y: Float) : IPoint {
    operator fun times(size: Size) = Point(x * size.width, y * size.height)
}

class MutablePoint() : IPoint {

    override var x: Float = 0f; private set
    override var y: Float = 0f; private set

    constructor(x: Float, y: Float) : this() {
        set(x, y)
    }

    fun set(x: Float = this.x, y: Float = this.y) {
        this.x = x
        this.y = y
    }
}