package com.kiylx.bus.eventbus.ipc.binder.base

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
import com.kiylx.bus.eventbus.core.OstensibleObserver
import com.kiylx.bus.eventbus.ipc.binder.interfaces.ChannelAction

class ObserverWrapper<T>( val observer: OstensibleObserver<T>,
                         private val lifecycleOwner: LifecycleOwner,
                         private val channelAction: ChannelAction) :LifecycleObserver{

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
   fun onDestroy(){
       channelAction?.destroyObserver()
    }

}