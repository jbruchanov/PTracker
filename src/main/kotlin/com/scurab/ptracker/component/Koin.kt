package com.scurab.ptracker.component

import com.scurab.ptracker.App.getKoin
import com.scurab.ptracker.component.navigation.ComponentFactory
import com.scurab.ptracker.component.navigation.LifecycleComponent
import org.koin.core.Koin
import org.koin.core.parameter.parametersOf
import kotlin.reflect.KClass

inline fun <reified T> get(): T = getKoin().get(T::class)

class KoinViewModelFactory(private val koin: Koin = getKoin()) : ComponentFactory {
    override fun <C : LifecycleComponent, T> create(klass: KClass<out C>, args: T): C {
        val params = parametersOf(args)
        return koin.get(klass, parameters = { params }) as C
    }
}