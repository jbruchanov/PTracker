package com.scurab.ptracker.component.navigation

import kotlin.reflect.KClass

interface ComponentFactory {
    fun <LC : LifecycleComponent, T> create(klass: KClass<out LC>, args: T): LC
}
