package com.scurab.ptracker.ext

import kotlin.math.roundToInt

val Float.f0 get() = this.roundToInt().toString()
val Float.f3 get() = String.format("%.3f", this)