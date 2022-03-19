package com.scurab.ptracker.component.navigation

interface NavController {
    fun push(key: String) = push(StringNavToken(key))
    fun push(token: NavToken<EmptyNavArgs>) = push(token, EmptyNavArgs)
    fun <T : NavArgs> push(token: NavToken<T>, args: T)
    fun pop(): Boolean
}