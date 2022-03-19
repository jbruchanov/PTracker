package com.scurab.ptracker.component

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlin.coroutines.CoroutineContext

open class ViewModel : CoroutineScope {
    private val job = Job()
    override val coroutineContext: CoroutineContext = job + Dispatchers.Default

    open fun start() {
        println("Start:$this")
    }

    open fun stop() {
        println("Stop:$this")
    }

    open fun destroy() {
        job.cancel()
        println("Destroy:$this")
    }
}