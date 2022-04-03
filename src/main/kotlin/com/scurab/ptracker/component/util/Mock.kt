package com.scurab.ptracker.component.util

import java.lang.reflect.InvocationHandler
import java.lang.reflect.Proxy
import kotlin.reflect.KClass

fun <T : Any> KClass<T>.mock(handler: InvocationHandler = InvocationHandler { _, _, _ -> }): T =
    Proxy.newProxyInstance(Thread.currentThread().contextClassLoader, arrayOf(this@mock.java), handler) as T
