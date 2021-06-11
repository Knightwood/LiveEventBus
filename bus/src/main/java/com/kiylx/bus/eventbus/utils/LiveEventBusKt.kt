package com.kiylx.bus.eventbus.utils

import com.kiylx.bus.eventbus.BusCore
import com.kiylx.bus.eventbus.core.Channel
import com.kiylx.bus.eventbus.core.interfaces.Mode

/**
 * 创建者 kiylx
 * 创建时间 2020/12/31 20:30
 * packageName：com.kiylx.liveeventbus.live_event_bus
 * 描述：
 */
object LiveEventBusKt {
    fun <T> with(channelName: String?): Channel<T> {
        return BusCore.getInstance().get(channelName)
    }

    fun <T> withCrossProcess(channelName: String?): Channel<T> {
        return BusCore.getInstance().get<T>(channelName)
                .config()
                .setIsUseCrossProcess(Mode.binder)
                .build()
    }

    fun <T> withCrossProcess(channelName: String?, mode: Mode?): Channel<T> {
        return BusCore.getInstance().get<T>(channelName)
                .config()
                .setIsUseCrossProcess(mode!!)
                .build()
    }
}