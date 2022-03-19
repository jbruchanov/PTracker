package com.scurab.ptracker.component.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.scurab.ptracker.ext.peekOrNull
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Stack

interface NavSpecs {
    val activeScreen: StateFlow<NavigationToken<*>>

    @Composable
    fun render()
}


class DefaultNavSpecs(
    items: List<NavRecord<*, *>>,
    private val viewModelFactory: ViewModelFactory,
    appArgs: AppArgs
) : NavSpecs, NavController {
    private val navItems = items.toList()
    private val stack: Stack<ActiveNavRecord<*>> = Stack()
    private val _activeScreenTokens = MutableStateFlow<NavigationToken<*>>(InitNavigationToken)
    override val activeScreen = _activeScreenTokens.asStateFlow()

    init {
        requireNotNull(navItems.firstOrNull { it.navigationToken == StartNavigationToken }) {
            "NavItems must include at least 1 record using StartNavigationToken"
        }
        push(StartNavigationToken, appArgs)
    }

    @Composable
    override fun render() {
        val token by activeScreen.collectAsState()
        println(token)
        stack.peek()?.render()
    }

    override fun <T : NavArgs> push(token: NavigationToken<T>, args: T) {
        val navItem = requireNotNull(navItems.firstOrNull { it.navigationToken == token }) {
            "Unable to find navItem matching token:$token"
        } as NavRecord<T, *>
        stack.peekOrNull()?.viewModel?.stop()

        val activeRecord = navItem.createActiveRecord(viewModelFactory, args)
        activeRecord.viewModel.start()
        stack.push(activeRecord)
        _activeScreenTokens.tryEmit(token)
    }

    override fun pop(): Boolean {
        val result = stack.size > 1
        if (result) {
            stack.pop().viewModel.apply {
                stop()
                destroy()
            }
            val peek = stack.peek()
            peek.viewModel.start()
            _activeScreenTokens.tryEmit(peek.navigationRecord.navigationToken)
        }
        return result
    }

    companion object {
        private object InitNavigationToken : NavigationToken<EmptyArgs>
    }
}