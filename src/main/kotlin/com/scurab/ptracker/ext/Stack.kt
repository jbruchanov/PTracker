package com.scurab.ptracker.ext

import java.util.Stack

fun <T> Stack<T>.peekOrNull(): T? = if (isNotEmpty()) peek() else null