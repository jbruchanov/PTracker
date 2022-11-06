package com.scurab.ptracker.component.navigation

import androidx.compose.animation.Crossfade
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateListOf

interface NavSpecs {
    val activeScreen: State<NavToken<out NavArgs>>
    val activeScreenNavToken: NavToken<*>

    @Composable
    fun render()

    @Composable
    operator fun invoke() = render()
}

class DefaultNavSpecs(
    items: List<NavRecord<*, *>>, private val componentFactory: ComponentFactory, private val appNavArgs: AppNavArgs
) : NavSpecs, NavController {
    private val navItems = items.toList()
    private val stack = mutableStateListOf<ActiveNavRecord<*>>()
    private val _activeScreenToken = derivedStateOf { stack.lastOrNull()?.navigationRecord?.navToken ?: InitNavToken }
    override val activeScreen = _activeScreenToken

    override val activeRecords: Int get() = stack.size
    override val activeScreenNavToken: NavToken<*> get() = stack.last().navigationRecord.navToken

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

        Crossfade(stack.lastOrNull()) {
            it?.render()
        }
    }

    override fun <T : NavArgs> push(token: NavToken<T>, args: T) {
        stopLast(required = false)
        addRecord(token, args)
        startLast(required = true)
    }

    override fun pop(steps: Int): Int {
        if (steps <= 0) return 0
        val popped = popAndDestroy(steps)
        startLast(required = false)
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
        startLast(required = true)
    }

    private fun popAndDestroy(steps: Int): Int {
        var counter = 0
        while (stack.size > 0 && counter < steps) {
            stack.removeLast().lifecycleComponent.apply {
                stop()
                destroy()
            }
            counter++
        }
        return counter
    }

    private fun startLast(required: Boolean) {
        val peek = if (required) stack.last() else stack.lastOrNull()
        peek?.lifecycleComponent?.start()
    }

    private fun stopLast(required: Boolean) {
        val record = if (required) stack.last() else stack.lastOrNull()
        record?.lifecycleComponent?.stop()
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T : NavArgs> addRecord(token: NavToken<T>, args: T) {
        val navItem = requireNotNull(navItems.firstOrNull { it.navToken == token }) {
            "Unable to find navItem matching token:$token"
        } as NavRecord<T, *>
        val activeRecord = navItem.createActiveRecord(componentFactory, args)
        stack.add(activeRecord)
    }

    companion object {
        private object InitNavToken : NavToken<EmptyNavArgs>
    }
}