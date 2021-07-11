package com.kiylx.bus.eventbus.ipc.binder.local_send

import androidx.lifecycle.LifecycleOwner
import com.kiylx.bus.eventbus.core.ChannelX
import com.kiylx.bus.eventbus.core.MainBusManager
import com.kiylx.bus.eventbus.core.interfaces.CrossProcessAction
import com.kiylx.bus.eventbus.ipc.binder.util.appServiceName
import com.kiylx.bus.eventbus.ipc.binder.util.currentProcessName
import com.kiylx.bus.eventbus.ipc.binder.util.remoteProcessName
import kotlinx.coroutines.launch


/**
 * @param <T>    消息通道的泛型类
 * @param target 消息通道名称
 * @param lifecycleOwner 生命周期，比如让channel跟随某个activity的lifecycle，destroy时不再发送消息。默认传入null，让channel跟随BusManager的生命周期
 * 控制消息通道的生命周期。null时，消息通道默认的生命周期是BusCore控制
 * @return 返回消息通道
</T> */
fun <T : Any> MainBusManager.getCrossProcessChannel(
    target: String,
    lifecycleOwner: LifecycleOwner? = null,
    clazz: Class<T>
): ChannelX<T> {
    if (!mChannels.containsKey(target)) {
        val channel = ChannelX<T>(target, clazz = clazz, getExtension() as CrossProcessAction?)
        if (lifecycleOwner == null)
            lifecycleRegistry.addObserver(channel)
        else
            lifecycleOwner.lifecycle.addObserver(channel)
        mChannels[target] = channel
    }
    return mChannels[target] as ChannelX<T>
}

fun MainBusManager.getExtension(): Any? {
    if (extention == null) {
        extention = MainBusManagerExtension.INSTANCE
    }
    return extention
}


/**
 * 把数据发送到服务端
 * @param value 要发送的数据
 * @param notifyLocal 发送到服务端的同时，是否同时发送到本地channel
 */
fun <T : Any> ChannelX<T>.postCrossProcess(
    value: T,
    notifyLocal: Boolean = false,
    dataFrom: String = currentProcessName,
    dataTo: String = "null", //不为“null”时，发送到这个指定的进程
    thisChannelName: String = channelName,
    connectService: String = appServiceName,
    dataType: String = " ",
) {
    if (notifyLocal) {
        post(value)
    }
    launch(coroutineContext) {
        val str = dataConvertToJson(value)
        crossProcessAction?.postDataToService<T>(
            str,
            dataFrom,
            dataTo,
            thisChannelName,
            connectService,
            dataType
        )

    }

}