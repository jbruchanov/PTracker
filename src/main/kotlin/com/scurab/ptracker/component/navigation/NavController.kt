package com.scurab.ptracker.component.navigation

interface NavController {
    fun push(key: String) = push(StringNavigationToken(key))
    fun push(token: NavigationToken<EmptyArgs>) = push(token, EmptyArgs)
    fun <T : NavArgs> push(token: NavigationToken<T>, args: T)
    fun pop(): Boolean
}