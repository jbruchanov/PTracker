package com.scurab.ptracker.component

import com.scurab.ptracker.component.navigation.LifecycleComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlin.coroutines.CoroutineContext

open class ViewModel : CoroutineScope, LifecycleComponent {
    private val job = Job()
    override val coroutineContext: CoroutineContext = job + Dispatchers.Default

    override fun start() {
        println("Start:$this")
    }

    override fun stop() {
        println("Stop:$this")
    }

    override fun destroy() {
        job.cancel()
        println("Destroy:$this")
    }
}