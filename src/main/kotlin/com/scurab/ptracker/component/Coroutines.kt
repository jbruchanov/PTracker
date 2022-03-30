package com.scurab.ptracker.component

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job

val ProcessScope: CoroutineScope = CoroutineScope(Job() + Dispatchers.Default)