package com.scurab.ptracker.component.navigation

import androidx.compose.runtime.Composable
import com.scurab.ptracker.component.ViewModel
import kotlin.reflect.KClass

fun navigation(
    viewModelFactory: ViewModelFactory,
    builder: NavSpecsBuilder.() -> Unit
): DefaultNavSpecs = NavSpecsBuilder(viewModelFactory).apply(builder).build()

open class NavSpecsBuilder(private val viewModelFactory: ViewModelFactory) {
    private val navElements = mutableListOf<NavRecord<*, *>>()
    private var appArgs = AppArgs(emptyArray())

    open fun build() = DefaultNavSpecs(navElements, viewModelFactory, appArgs)

    open fun appArgs(args: Array<String>) {
        appArgs = AppArgs(args)
    }

    open fun <VM : ViewModel, T : NavArgs> register(navigationRecord: NavRecord<T, VM>) {
        navElements.add(navigationRecord)
    }

    open fun <VM : ViewModel, T : NavArgs> screen(navigationToken: NavigationToken<T>, viewModelKClass: KClass<VM>, content: @Composable (VM) -> Unit) {
        register(NavRecord(navigationToken, viewModelKClass, content))
    }

    inline fun <reified VM : ViewModel, T : NavArgs> screen(navigationToken: NavigationToken<T>, noinline content: @Composable (VM) -> Unit) {
        register(NavRecord(navigationToken, VM::class, content))
    }

    inline fun <reified VM : ViewModel> screen(key: String, noinline content: @Composable (VM) -> Unit) {
        register(NavRecord(StringNavigationToken(key), VM::class, content))
    }
}

class NavRecord<T : NavArgs, VM : ViewModel>(
    val navigationToken: NavigationToken<T>,
    val viewModelClass: KClass<out VM>,
    val content: @Composable (VM) -> Unit
) {
    fun createActiveRecord(viewModelFactory: ViewModelFactory, args: T): ActiveNavRecord<VM> {
        return ActiveNavRecord(viewModelFactory.create(viewModelClass, args), this)
    }
}

class ActiveNavRecord<VM : ViewModel>(
    val viewModel: VM, val navigationRecord: NavRecord<*, VM>
) {
    @Composable
    fun render() {
        navigationRecord.content(viewModel)
    }
}