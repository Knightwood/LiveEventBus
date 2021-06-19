package com.kiylx.bus.eventbus.ipc.binder.services

import com.kiylx.bus.eventbus.IClientListener
import com.kiylx.bus.eventbus.core.Channel
import com.kiylx.bus.eventbus.core.MainBusManager
import com.kiylx.bus.eventbus.core.OstensibleObserver
import com.kiylx.bus.eventbus.ipc.binder.generateMessage
import com.kiylx.bus.eventbus.ipc.binder.generateMsgFromString
import com.kiylx.bus.eventbus.ipc.binder.model.ChannelConnectInfo
import com.kiylx.bus.eventbus.ipc.binder.model.ConnectResult
import com.kiylx.bus.eventbus.ipc.binder.model.EventMessage
import com.kiylx.bus.eventbus.ipc.binder.model.ResultCode
import kotlinx.coroutines.ObsoleteCoroutinesApi

class ProcessManager private constructor() {
    private val locateProcessList: MutableMap<String, ProcessWrapper> by lazy { mutableMapOf() }
    private var mainBusManager: MainBusManager? = null

    init {
        mainBusManager = MainBusManager.instance
    }

    fun registerLocate(listener: IClientListener?) {
        listener?.let {
            val locateFrom: String = it.locateFrom
            if (!locateProcessList.containsKey(locateFrom)) {
                locateProcessList[locateFrom] = ProcessWrapper(it)
            }
        }

    }

    fun unregisterLocate(listener: IClientListener?) {
        listener?.let {
            val locateFrom: String = it.locateFrom
            if (!locateProcessList.containsKey(locateFrom)) {
                locateProcessList.remove(locateFrom)
            }
        }
    }

    @ObsoleteCoroutinesApi
    fun dispatchMsgToChannel(message: EventMessage?) {
        message?.run {
            val channelName = channel
            mainBusManager?.getChannel2(channelName)?.post(generateMsgFromString(json))
        }
    }


    fun unListenChannel(connectInfo: ChannelConnectInfo?) {
        connectInfo?.run {
            val channelName: String = channelName
            val channel: Channel<Any>? = mainBusManager?.getChannel2(channelName)
            if (channel != null) {
                val locateWrapper = locateProcessList[locateFrom]
                locateWrapper?.getObserver(channelName)?.let { channel.removeObserver(it) }
                locateWrapper?.removeObserver(channelName)
            }
        }
    }

    fun listenChannel(connectInfo: ChannelConnectInfo?): ConnectResult {
        connectInfo?.run {
            val channelName: String = channelName
            val channel: Channel<Any>? = mainBusManager?.getChannel2(channelName)
            return if (channel != null) {
                val locateWrapper = locateProcessList[locateFrom]
                val tmp = object : OstensibleObserver<Any>() {
                    override fun onChanged(t: Any) {
                        locateWrapper?.client?.notifyDataChanged(generateMessage(connectInfo, t))
                    }
                }
                locateWrapper?.addObserver(channelName, tmp)

                channel.observeForever(ostensibleObserver = tmp)
                ConnectResult(ResultCode.success)
            } else {
                ConnectResult(ResultCode.channelNotFound)
            }
        }
        return ConnectResult(ResultCode.connectFailed, "ChannelConnectInfo为null")
    }

    fun getMessage(connectInfo: ChannelConnectInfo?): EventMessage {
        connectInfo?.run {
            val channelName: String = channelName
            val channel: Channel<Any>? = mainBusManager?.getChannel2(channelName)
            if (channel != null) {
                return EventMessage(" ", " ", " ", " ", " ", channel.convertToJson())
            }
        }
        return EventMessage()
    }

    fun destroy() {
        mainBusManager = null
    }


    companion object {
        @JvmStatic
        val instance: ProcessManager by lazy(LazyThreadSafetyMode.SYNCHRONIZED) { ProcessManager() }

    }
}

/**
 *一个实例就代表服务端与一个客户端的连接，里面存储这个客户端对服务端channel监听的observer
 */
class ProcessWrapper(var client: IClientListener) {
    private val listenChannelsMap: MutableMap<String, OstensibleObserver<Any>> by lazy { mutableMapOf() }

    fun getObserver(channelName: String): OstensibleObserver<Any>? {
        return listenChannelsMap[channelName]
    }

    fun addObserver(channelName: String, tmp: OstensibleObserver<Any>) {
        listenChannelsMap[channelName] = tmp
    }

    fun removeObserver(channelName: String) {
        listenChannelsMap.remove(channelName)
    }

}