package com.scurab.ptracker.component.compose

import androidx.compose.ui.graphics.Color

data class StateColor(
    val default: Color,
    val disabled: Color = default,
    val selected: Color = default,
) {
    fun get(
        isEnabled: Boolean = true, isSelected: Boolean = false
    ) = when {
        isEnabled && !isSelected -> default
        isEnabled && isSelected -> selected
        !isEnabled -> disabled
        else -> default
    }
}