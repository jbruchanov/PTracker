package com.scurab.ptracker.component.delegate

import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KMutableProperty0
import kotlin.reflect.KProperty

typealias OnKeyChangeListener = (String) -> Unit

class NotifyingReadWriteProperty<T>(
    private val kMutableProperty0: KMutableProperty0<T>,
    private val onChangeRef: () -> OnKeyChangeListener
) : ReadWriteProperty<Any, T> {

    override fun getValue(thisRef: Any, property: KProperty<*>): T {
        return kMutableProperty0.get()
    }

    override fun setValue(thisRef: Any, property: KProperty<*>, value: T) {
        val oldValue = kMutableProperty0.get()
        kMutableProperty0.set(value)
        if (oldValue != value) {
            onChangeRef()(property.name)
        }
    }
}
