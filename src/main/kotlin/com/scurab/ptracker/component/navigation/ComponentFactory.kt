package com.scurab.ptracker.component.navigation

import kotlin.reflect.KClass

interface ComponentFactory {
    fun <VM : LifecycleComponent, T> create(klass: KClass<out VM>, args: T): VM
}