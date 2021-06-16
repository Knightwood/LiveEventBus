package com.kiylx.bus.eventbus.ipc.binder.base

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
import com.kiylx.bus.eventbus.core.OstensibleObserver
import com.kiylx.bus.eventbus.ipc.binder.interfaces.ChannelAction

class ObserverWrapper<T>(val observer: OstensibleObserver<T>,
                         lifecycleOwner: LifecycleOwner,
                         channelAction: ChannelAction) : LifecycleObserver {
    private var mChannelAction: ChannelAction? = null
    var firstNotify = true

    fun notify(data: T) {
        if (observer.config().isWantAcceptMessage)
            observer.onChanged(data)
    }

    init {
        lifecycleOwner.lifecycle.addObserver(this@ObserverWrapper)
        mChannelAction = channelAction
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun onDestroy() {
        mChannelAction?.destroyObserver(observer.uuid)
        mChannelAction = null
    }

}