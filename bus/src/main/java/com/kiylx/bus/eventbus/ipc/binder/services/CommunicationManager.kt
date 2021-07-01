package com.kiylx.bus.eventbus.ipc.binder.services

import android.os.RemoteCallbackList
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
 * 应用本身是单进程可使用此类
 */
class CommunicationManager private constructor() : Cornerstone() {
    //<locateFrom,ProcessWrapper>  locateFrom:来自哪个进程,客户端进程的名称
    private val locateProcessList: MutableMap<String, ProcessWrapper> by lazy { mutableMapOf() }//应用外的进程连接
    private var mainBusManager: MainBusManager? = null
    private val callbackList:RemoteCallbackList<IClientListener> = RemoteCallbackList<IClientListener>()

    init {
        mainBusManager = MainBusManager.instance
    }

    fun registerLocate(listener: IClientListener?) {
        listener?.let {
            val locateFrom: String = it.locateFrom
            if (!locateProcessList.containsKey(locateFrom)) {
                locateProcessList[locateFrom] = ProcessWrapper(it)
                callbackList.register(listener)
            }
        }

    }

    fun unregisterLocate(listener: IClientListener?) {
        listener?.let {
            val locateFrom: String = it.locateFrom
            if (!locateProcessList.containsKey(locateFrom)) {
                locateProcessList.remove(locateFrom)
                callbackList.unregister(listener)
            }
        }
    }

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
    private suspend fun getMessage(connectInfo: ChannelConnectInfo?): EventMessage {
        return if (connectInfo == null) {
            EventMessage(" ", "connectInfo为null", " ")
        } else {
            val channelName: String = connectInfo.channelName
            val channelX: ChannelX<Any>? = mainBusManager?.getChannel2(channelName)
            if (channelX != null) {
                EventMessage(channelName, " ", channelX.dataConvertToJson())
            } else {
                EventMessage(channelName, "channel不存在", " ")
            }
        }
    }

    override fun clear() {
        super.clear()
        mainBusManager = null
        callbackList.kill()
        locateProcessList.clear()
    }

    companion object {
        @JvmStatic
        val instance: CommunicationManager by lazy(LazyThreadSafetyMode.SYNCHRONIZED) { CommunicationManager() }

    }
}