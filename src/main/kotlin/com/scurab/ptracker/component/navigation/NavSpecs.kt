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
    val activeScreenNavToken: NavToken<*>

    @Composable
    fun render()
}

class DefaultNavSpecs(
    items: List<NavRecord<*, *>>, private val componentFactory: ComponentFactory, private val appNavArgs: AppNavArgs
) : NavSpecs, NavController {
    private val navItems = items.toList()
    private val stack: Stack<ActiveNavRecord<*>> = Stack()
    private val _activeScreenTokens = MutableStateFlow<NavToken<*>>(InitNavToken)
    override val activeScreen = _activeScreenTokens.asStateFlow()

    override val activeRecords: Int get() = stack.size
    override val activeScreenNavToken: NavToken<*> get() = stack.peek().navigationRecord.navToken

    private var needInitRecord = true

    init {
        //push at this point can't be done as it's potentially creating infinite loop
        //Start -> build navs -> push Start -> Start depends on this NavController -> DI -> build navs again
    }

    private fun init() {
        if (stack.isEmpty()) {
            val record = navItems.firstOrNull { it.navToken is StartNavToken }
            requireNotNull(record) {
                "NavItems must include at least 1 record using StartNavToken"
            }
            push(record.navToken as StartNavToken, appNavArgs)
        }
    }

    @Composable
    override fun render() {
        if (needInitRecord) {
            needInitRecord = false
            init()
        }
        val token by activeScreen.collectAsState()
        //reading of token necessary to make it working, otherwise recomposition not triggered
        println(token)
        stack.peek()?.render()
    }

    override fun <T : NavArgs> push(token: NavToken<T>, args: T) {
        stopPeek(required = false)
        addRecord(token, args)
        startPeek()
        notifyPeekStackChanged()
    }

    override fun pop(steps: Int): Int {
        if (steps <= 0) return 0
        val popped = popAndDestroy(steps)
        startPeek()
        notifyPeekStackChanged()
        return popped
    }

    override fun <T : NavArgs> replace(token: NavToken<T>, args: T) {
        replace(removeRecords = 1, token, args)
    }

    override fun <T : NavArgs> replaceAll(token: NavToken<T>, args: T) {
        replace(Int.MAX_VALUE, token, args)
    }

    override fun popTo(token: NavToken<*>): Int {
        val i = stack.indexOfFirst { it.navigationRecord.navToken == token }
        return pop(stack.size - i - 1)
    }

    private fun <T : NavArgs> replace(removeRecords: Int, token: NavToken<T>, args: T) {
        popAndDestroy(steps = removeRecords)
        addRecord(token, args)
        startPeek()
        notifyPeekStackChanged()
    }

    private fun notifyPeekStackChanged() {
        _activeScreenTokens.tryEmit(stack.peek().navigationRecord.navToken)
    }

    private fun popAndDestroy(steps: Int): Int {
        var counter = 0
        while (stack.size > 0 && counter < steps) {
            stack.pop().lifecycleComponent.apply {
                stop()
                destroy()
            }
            counter++
        }
        return counter
    }

    private fun startPeek() {
        val peek = stack.peek()
        peek.lifecycleComponent.start()
    }

    private fun stopPeek(required: Boolean) {
        val record = if (required) stack.peek() else stack.peekOrNull()
        record?.lifecycleComponent?.stop()
    }

    private fun <T : NavArgs> addRecord(token: NavToken<T>, args: T) {
        val navItem = requireNotNull(navItems.firstOrNull { it.navToken == token }) {
            "Unable to find navItem matching token:$token"
        } as NavRecord<T, *>
        val activeRecord = navItem.createActiveRecord(componentFactory, args)
        stack.push(activeRecord)
    }

    companion object {
        private object InitNavToken : NavToken<EmptyNavArgs>
    }
}