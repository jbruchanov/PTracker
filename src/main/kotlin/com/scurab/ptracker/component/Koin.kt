package com.scurab.ptracker.component

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.structuralEqualityPolicy
import com.scurab.ptracker.App.getKoin

val LocalKoin = compositionLocalOf(structuralEqualityPolicy()) { getKoin() }

inline fun <reified T> get(): T = getKoin().get(T::class)