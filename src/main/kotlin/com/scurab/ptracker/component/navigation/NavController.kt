package com.scurab.ptracker.component.navigation

interface NavController {
    val activeRecords: Int

    fun push(key: String) = push(StringNavToken(key))
    fun push(token: NavToken<EmptyNavArgs>) = push(token, EmptyNavArgs)
    fun <T : NavArgs> push(token: NavToken<T>, args: T)

    fun <T : NavArgs> replace(token: NavToken<T>, args: T)
    fun <T : NavArgs> replaceAll(token: NavToken<T>, args: T)

    fun pop(steps: Int = 1): Int
    fun popToTop() = pop(steps = activeRecords - 1)
    fun popTo(token: NavToken<*>): Int
}