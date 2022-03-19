package com.scurab.ptracker.component

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.structuralEqualityPolicy
import com.scurab.ptracker.App.getKoin
import com.scurab.ptracker.component.navigation.ViewModelFactory
import org.koin.core.Koin
import org.koin.core.parameter.parametersOf
import kotlin.reflect.KClass

val LocalKoin = compositionLocalOf(structuralEqualityPolicy()) { getKoin() }

inline fun <reified T> get(): T = getKoin().get(T::class)

class KoinViewModelFactory(private val koin: Koin = getKoin()) : ViewModelFactory {
    override fun <VM : ViewModel, T> create(viewModelClass: KClass<out VM>, args: T): VM {
        val params = parametersOf(args)
        return koin.get(viewModelClass, parameters = { params }) as VM
    }
}