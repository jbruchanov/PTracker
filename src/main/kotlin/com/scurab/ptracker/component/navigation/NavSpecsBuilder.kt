package com.scurab.ptracker.component.navigation

import androidx.compose.runtime.Composable
import kotlin.reflect.KClass

fun navigation(
    componentFactory: ComponentFactory,
    builder: NavSpecsBuilder.() -> Unit
): DefaultNavSpecs = NavSpecsBuilder(componentFactory).apply(builder).build()

open class NavSpecsBuilder(private val componentFactory: ComponentFactory) {
    private val navElements = mutableListOf<NavRecord<*, *>>()
    private var appNavArgs = AppNavArgs(emptyArray())

    open fun build() = DefaultNavSpecs(navElements, componentFactory, appNavArgs)

    open fun appArgs(args: Array<String>) {
        appNavArgs = AppNavArgs(args)
    }

    open fun <LC : LifecycleComponent, T : NavArgs> register(navigationRecord: NavRecord<T, LC>) {
        navElements.add(navigationRecord)
    }

    open fun <LC : LifecycleComponent, T : NavArgs> screen(navToken: NavToken<T>, viewModelKClass: KClass<LC>, content: @Composable (LC) -> Unit) {
        register(NavRecord(navToken, viewModelKClass, content))
    }

    inline fun <reified LC : LifecycleComponent, T : NavArgs> screen(navToken: NavToken<T>, noinline content: @Composable (LC) -> Unit) {
        register(NavRecord(navToken, LC::class, content))
    }
    inline fun <reified LC : LifecycleComponent> screen(key: String, noinline content: @Composable (LC) -> Unit) {
        register(NavRecord(StringNavToken(key), LC::class, content))
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