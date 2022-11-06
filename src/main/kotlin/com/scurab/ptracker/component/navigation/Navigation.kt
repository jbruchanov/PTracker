package com.scurab.ptracker.component.navigation

interface NavArgs
object EmptyNavArgs : NavArgs
open class AppNavArgs(val items: Array<String>) : NavArgs
object EmptyAppNavArgs : AppNavArgs(emptyArray())

interface NavToken<T : NavArgs>
interface StartNavToken : NavToken<AppNavArgs>
object DefaultStartNavToken : StartNavToken
data class StringNavToken(val key: String) : NavToken<EmptyNavArgs>
