package com.scurab.ptracker.component.compose

data class StateContainer<T>(
    val default: T,
    val disabled: T = default,
    val selected: T = default,
    val default2: T = default
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