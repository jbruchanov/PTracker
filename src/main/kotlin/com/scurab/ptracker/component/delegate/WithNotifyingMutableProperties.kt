package com.scurab.ptracker.component.delegate

import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KMutableProperty0

interface WithNotifyingMutableProperties {

    fun <V> KMutableProperty0<V>.notifying(): ReadWriteProperty<Any, V>

    fun WithNotifyingMutableProperties.setNotifyingCallback(onChange: OnKeyChangeListener)

    class Delegate : WithNotifyingMutableProperties {
        private var onChange: OnKeyChangeListener? = null
        private val onChangeRef: () -> OnKeyChangeListener = { requireNotNull(onChange) { "callback not set, have you called setNotifyingCallback?!" } }

        override fun <V> KMutableProperty0<V>.notifying(): ReadWriteProperty<Any, V> = NotifyingReadWriteProperty(this, onChangeRef)

        override fun WithNotifyingMutableProperties.setNotifyingCallback(onChange: OnKeyChangeListener) {
            this@Delegate.onChange = onChange
        }
    }
}
