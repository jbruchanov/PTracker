package com.scurab.ptracker.component.compose

import androidx.compose.ui.graphics.Color

data class StateColor(
    val default: Color,
    val disabled: Color = default,
    val selected: Color = default,
    val default2: Color = default
) {
    fun get(
        isEnabled: Boolean = true, isSelected: Boolean = false, isEven: Boolean = false
    ) = when {
        isEnabled && !isSelected && isEven -> default
        isEnabled && !isSelected && !isEven -> default2
        isEnabled && isSelected -> selected
        !isEnabled -> disabled
        else -> default
    }
}