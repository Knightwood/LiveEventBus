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
import kotlinx.coroutines.launch

class AppCommunicationManager private constructor() : Cornerstone() {
    //<locateFrom,ProcessWrapper>  locateFrom:来自哪个进程,客户端进程的名称
    private val appProcessList: MutableMap<String, ProcessWrapper> by lazy { mutableMapOf() }//应用外的进程连接
    private val callbackList: RemoteCallbackList<IClientListener> = RemoteCallbackList<IClientListener>()

    fun registerAppListener(listener: IClientListener?) {
        listener?.let {
            val locateFrom: String = it.locateFrom
            if (!appProcessList.containsKey(locateFrom)) {
                appProcessList[locateFrom] = ProcessWrapper(it)
                callbackList.register(listener)
            }
        }

    }

    fun unregisterAppListener(listener: IClientListener?) {
        listener?.let {
            val locateFrom: String = it.locateFrom
            if (!appProcessList.containsKey(locateFrom)) {
                appProcessList.remove(locateFrom)
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
                val locateWrapper = appProcessList[locateFrom]
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
                val locateWrapper = appProcessList[locateFrom]
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
                val processWrapper = appProcessList[it.locateFrom]
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
        appProcessList.clear()
    }

    companion object {
        @JvmStatic
        val instance: AppCommunicationManager by lazy(LazyThreadSafetyMode.SYNCHRONIZED) { AppCommunicationManager() }

    }
}
