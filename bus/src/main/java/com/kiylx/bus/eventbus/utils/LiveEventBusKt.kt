package com.kiylx.bus.eventbus.utils

import android.content.Context
import androidx.lifecycle.LifecycleOwner
import com.kiylx.bus.eventbus.core.MainBusManager
import com.kiylx.bus.eventbus.core.ChannelX
import com.kiylx.bus.eventbus.ipc.binder.local_receive.CrossProcessBusManager
import com.kiylx.bus.eventbus.ipc.binder.local_receive.CrossChannel
import com.kiylx.bus.eventbus.ipc.binder.model.ChannelConnectInfo

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
inline fun <reified T : Any> with(channelName: String, channelLifeCycle: LifecycleOwner? = null): ChannelX<T> {
    return MainBusManager.instance.getChannel<T>(channelName, channelLifeCycle, T::class.java)
}

inline fun <reified T : Any> withCrossProcess(context: Context, channelConnectInfo: ChannelConnectInfo): CrossChannel<T>? {
    return CrossProcessBusManager.instance.getChannel<T>(context, channelConnectInfo, T::class.java)
}

/*fun withCrossProcess(): BoardCastChannel {
    return CrossProcessBusManager.instance.getChannel()
            .config()
            .setIsUseCrossProcess(mode = Mode.broadcast)
            .build()
}*/
