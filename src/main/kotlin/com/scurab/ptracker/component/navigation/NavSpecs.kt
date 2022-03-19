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
    val activeScreen: StateFlow<NavToken<*>>

    @Composable
    fun render()
}

class DefaultNavSpecs(
    items: List<NavRecord<*, *>>,
    private val componentFactory: ComponentFactory,
    appNavArgs: AppNavArgs
) : NavSpecs, NavController {
    private val navItems = items.toList()
    private val stack: Stack<ActiveNavRecord<*>> = Stack()
    private val _activeScreenTokens = MutableStateFlow<NavToken<*>>(InitNavToken)
    override val activeScreen = _activeScreenTokens.asStateFlow()

    init {
        requireNotNull(navItems.firstOrNull { it.navToken == StartNavToken }) {
            "NavItems must include at least 1 record using StartNavigationToken"
        }
        push(StartNavToken, appNavArgs)
    }

    @Composable
    override fun render() {
        val token by activeScreen.collectAsState()
        println(token)
        stack.peek()?.render()
    }

    override fun <T : NavArgs> push(token: NavToken<T>, args: T) {
        val navItem = requireNotNull(navItems.firstOrNull { it.navToken == token }) {
            "Unable to find navItem matching token:$token"
        } as NavRecord<T, *>
        stack.peekOrNull()?.lifecycleComponent?.stop()

        val activeRecord = navItem.createActiveRecord(componentFactory, args)
        activeRecord.lifecycleComponent.start()
        stack.push(activeRecord)
        _activeScreenTokens.tryEmit(token)
    }

    override fun pop(): Boolean {
        val result = stack.size > 1
        if (result) {
            stack.pop().lifecycleComponent.apply {
                stop()
                destroy()
            }
            val peek = stack.peek()
            peek.lifecycleComponent.start()
            _activeScreenTokens.tryEmit(peek.navigationRecord.navToken)
        }
        return result
    }

    companion object {
        private object InitNavToken : NavToken<EmptyNavArgs>
    }
}