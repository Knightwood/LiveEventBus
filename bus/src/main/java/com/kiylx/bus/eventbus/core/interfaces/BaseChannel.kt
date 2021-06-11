package com.kiylx.bus.eventbus.core.interfaces

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.kiylx.bus.eventbus.core.Channel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import java.util.*
import kotlin.coroutines.CoroutineContext

abstract class BaseChannel: CoroutineScope, LifecycleObserver {
    val uuid: UUID = UUID.randomUUID()//通道标识符
    private val job: Job by lazy { Job() }
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    public fun clear() {
        job.cancel()
    }

    companion object {
        const val TAG = "消息通道"
    }
}