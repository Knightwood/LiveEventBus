package com.kiylx.bus.eventbus.utils

import androidx.lifecycle.LifecycleOwner
import com.kiylx.bus.eventbus.core.MainBusManager
import com.kiylx.bus.eventbus.core.Channel
import com.kiylx.bus.eventbus.core.interfaces.Mode
import com.kiylx.bus.eventbus.ipc.binder.CrossProcessBusManager
import com.kiylx.bus.eventbus.ipc.binder.CrossChannel
import com.kiylx.bus.eventbus.ipc.binder.model.ServiceConnectInfo
import com.kiylx.bus.eventbus.ipc.boardcast.BoardCastChannel

/**
 * 创建者 kiylx
 * 创建时间 2020/12/31 20:30
 * packageName：com.kiylx.liveeventbus.live_event_bus
 * 描述：
 */
/**
 * @param channelLifeCycle 默认可以不传。
 * 控制channel的生命周期
 */
fun <T> with(channelName: String, channelLifeCycle: LifecycleOwner? = null): Channel<T> {
    return MainBusManager.instance.getChannel<T>(channelName, channelLifeCycle)
}

fun withCrossProcess(serviceConnectInfo: ServiceConnectInfo): CrossChannel<Any?> {
    return CrossProcessBusManager.instance.getChannel(serviceConnectInfo)
            .config()
            .setIsUseCrossProcess(mode = Mode.binder)
            .build()
}

fun withCrossProcess(): BoardCastChannel {
    return CrossProcessBusManager.instance.getChannel()
            .config()
            .setIsUseCrossProcess(mode = Mode.broadcast)
            .build()
}
