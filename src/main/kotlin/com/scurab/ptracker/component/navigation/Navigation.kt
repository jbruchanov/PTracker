package com.scurab.ptracker.component.navigation


interface NavArgs
object EmptyNavArgs : NavArgs
class AppNavArgs(val items: Array<String>) : NavArgs

interface NavToken<T : NavArgs>
object StartNavToken : NavToken<AppNavArgs>
data class StringNavToken(val key: String) : NavToken<EmptyNavArgs>

