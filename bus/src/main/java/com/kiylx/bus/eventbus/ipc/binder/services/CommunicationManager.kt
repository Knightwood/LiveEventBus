package com.kiylx.bus.eventbus.ipc.binder.services

import com.kiylx.bus.eventbus.IClientListener
import com.kiylx.bus.eventbus.core.ChannelX
import com.kiylx.bus.eventbus.core.MainBusManager
import com.kiylx.bus.eventbus.core.OstensibleObserver
import com.kiylx.bus.eventbus.core.interfaces.Cornerstone
import com.kiylx.bus.eventbus.core.interfaces.Mode
import com.kiylx.bus.eventbus.ipc.binder.model.ChannelConnectInfo
import com.kiylx.bus.eventbus.ipc.binder.model.ConnectResult
import com.kiylx.bus.eventbus.ipc.binder.model.EventMessage
import com.kiylx.bus.eventbus.ipc.binder.model.ResultCode
import kotlinx.coroutines.*

/**
 * 服务端实现的方法在此类中得以实现，servcie将方法委托给此类。
 */
class CommunicationManager private constructor() : Cornerstone() {
    //<locateFrom,ProcessWrapper>  locateFrom:来自哪个进程,客户端进程的名称
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
            mainBusManager?.getChannel2(channelName)?.postJson(json)
        }
    }


    fun unListenChannel(connectInfo: ChannelConnectInfo?) {
        connectInfo?.run {
            val channelName: String = channelName
            val channelX: ChannelX<Any>? = mainBusManager?.getChannel2(channelName)
            if (channelX != null) {
                val locateWrapper = locateProcessList[locateFrom]
                locateWrapper?.findLocalObserver(channelName)?.let { channelX.removeObserver(it) }
                locateWrapper?.deleteLocalObserver(channelName)
            }
        }
    }

    /**
     * 添加一个监听某个channel的observer到服务端
     */
    fun listenChannel(connectInfo: ChannelConnectInfo?): ConnectResult {
        connectInfo?.run {
            val channelName: String = channelName
            val channelX: ChannelX<Any>? = mainBusManager?.getChannel2(channelName)
            return if (channelX != null) {
                //客户端所需要的服务端channel存在
                val locateWrapper = locateProcessList[locateFrom]
                val tmp = object : OstensibleObserver<Any>() {
                    override fun onChanged(t: Any) {
                        locateWrapper?.client?.notifyDataChanged(EventMessage(" ", " ", t.toString()))
                    }
                }.config().setCrossProcess(mode = Mode.binder).build()
                channelX.observeForever(ostensibleObserver = tmp)//添加监听
                locateWrapper?.storeLocalObserver(channelName, tmp)

                launch(coroutineContext) {
                    tmp.onChanged(EventMessage(" ", " ", channelX.dataConvertToJson()))
                }

                ConnectResult(ResultCode.success)
            } else {
                ConnectResult(ResultCode.channelNotFound)
            }
        }
        return ConnectResult(ResultCode.connectFailed, "ChannelConnectInfo为null")
    }

    fun sendMesToClient(connectInfo: ChannelConnectInfo?) {
        connectInfo?.let {
            launch(coroutineContext) {
                val message = getMessage(it)
                val processWrapper = locateProcessList[it.locateFrom]
                processWrapper?.client?.notifyDataChanged(message)
            }
        }
    }

    /**
     * 从服务端特定channel中拿取最新消息
     */
    suspend fun getMessage(connectInfo: ChannelConnectInfo?): EventMessage {
        var data: EventMessage
        if (connectInfo == null) {
            data = EventMessage()
        } else {
            val channelName: String = connectInfo.channelName
            val channelX: ChannelX<Any>? = mainBusManager?.getChannel2(channelName)
            if (channelX != null) {
                data = EventMessage(channelName, " ", channelX.dataConvertToJson())
            } else {
                data = EventMessage(channelName, "channel不存在", " ")
            }
        }
        return data
    }

    override fun clear() {
        super.clear()
        mainBusManager = null
    }

    companion object {
        @JvmStatic
        val instance: CommunicationManager by lazy(LazyThreadSafetyMode.SYNCHRONIZED) { CommunicationManager() }

    }
}

/**
 *一个实例就代表服务端与一个客户端的连接，里面存储这个客户端对服务端的一个channel监听的observer
 */
class ProcessWrapper(var client: IClientListener) {
    private val listenChannelsMap: MutableMap<String, OstensibleObserver<Any>> by lazy { mutableMapOf() }

    fun findLocalObserver(channelName: String): OstensibleObserver<Any>? {
        return listenChannelsMap[channelName]
    }

    /**
     * 将本地observer存储起来
     */
    fun storeLocalObserver(channelName: String, tmp: OstensibleObserver<Any>) {
        listenChannelsMap[channelName] = tmp
    }

    fun deleteLocalObserver(channelName: String) {
        listenChannelsMap.remove(channelName)
    }

}