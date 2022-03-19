package com.scurab.ptracker.component.navigation

import androidx.compose.runtime.Composable
import com.scurab.ptracker.component.ViewModel
import kotlin.reflect.KClass

fun navigation(
    viewModelFactory: ComponentFactory,
    builder: NavSpecsBuilder.() -> Unit
): DefaultNavSpecs = NavSpecsBuilder(viewModelFactory).apply(builder).build()

open class NavSpecsBuilder(private val viewModelFactory: ComponentFactory) {
    private val navElements = mutableListOf<NavRecord<*, *>>()
    private var appNavArgs = AppNavArgs(emptyArray())

    open fun build() = DefaultNavSpecs(navElements, viewModelFactory, appNavArgs)

    open fun appArgs(args: Array<String>) {
        appNavArgs = AppNavArgs(args)
    }

    open fun <VM : ViewModel, T : NavArgs> register(navigationRecord: NavRecord<T, VM>) {
        navElements.add(navigationRecord)
    }

    open fun <VM : ViewModel, T : NavArgs> screen(navToken: NavToken<T>, viewModelKClass: KClass<VM>, content: @Composable (VM) -> Unit) {
        register(NavRecord(navToken, viewModelKClass, content))
    }

    inline fun <reified VM : ViewModel, T : NavArgs> screen(navToken: NavToken<T>, noinline content: @Composable (VM) -> Unit) {
        register(NavRecord(navToken, VM::class, content))
    }

    inline fun <reified VM : ViewModel> screen(key: String, noinline content: @Composable (VM) -> Unit) {
        register(NavRecord(StringNavToken(key), VM::class, content))
    }
}

class NavRecord<T : NavArgs, C : LifecycleComponent>(
    val navToken: NavToken<T>,
    val klass: KClass<out C>,
    val content: @Composable (C) -> Unit
) {
    fun createActiveRecord(componentFactory: ComponentFactory, args: T): ActiveNavRecord<C> {
        return ActiveNavRecord(componentFactory.create(klass, args), this)
    }
}

class ActiveNavRecord<C : LifecycleComponent>(
    val lifecycleComponent: C,
    val navigationRecord: NavRecord<*, C>
) {
    @Composable
    fun render() {
        navigationRecord.content(lifecycleComponent)
    }
}