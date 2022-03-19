package com.scurab.ptracker.component.navigation


interface NavArgs
object EmptyArgs : NavArgs

interface NavigationToken<T : NavArgs>
object StartNavigationToken : NavigationToken<AppArgs>

data class ScreenArgs(val item: String) : NavArgs
class AppArgs(val items: Array<String>) : NavArgs
data class StringNavigationToken(val key: String) : NavigationToken<EmptyArgs>
data class StringArgs(val value: String) : NavArgs

