package com.scurab.ptracker.component.navigation

import com.scurab.ptracker.component.ViewModel
import kotlin.reflect.KClass

interface ViewModelFactory {
    fun <VM : ViewModel, T> create(viewModelClass: KClass<out VM>, args: T): VM
}