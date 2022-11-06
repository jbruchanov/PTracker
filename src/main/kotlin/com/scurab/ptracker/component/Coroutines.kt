package com.scurab.ptracker.component

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

val ProcessScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
