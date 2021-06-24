package com.kiylx.bus.eventbus.core.interfaces

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import java.util.*
import kotlin.coroutines.CoroutineContext

abstract class Cornerstone2 : CoroutineScope, LifecycleObserver {
    val uuid: UUID = UUID.randomUUID()//通道标识符
    private val job: Job by lazy { Job() }
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Default + job

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    open fun clear() {
        job.cancel()
    }

    companion object {
        const val TAG = "消息通道"
    }
}