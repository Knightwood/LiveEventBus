package com.kiylx.bus.eventbus.ipc.binder.services

import com.kiylx.bus.eventbus.IAppLocalInterface
import com.kiylx.bus.eventbus.IClientListener
import com.kiylx.bus.eventbus.core.interfaces.Cornerstone
import com.kiylx.bus.eventbus.ipc.binder.model.*
import com.kiylx.bus.eventbus.ipc.binder.util.ListTab
import com.kiylx.bus.eventbus.utils.Logs
import kotlinx.coroutines.launch
import java.util.*

class HostClientManager private constructor() : Cornerstone() {
    var roots: MutableMap<String, HostTab> = mutableMapOf()//<hostProcessName,hostTree>
    var clients: MutableMap<String, ClientBean> = mutableMapOf()//<clientProcessName,ClientBean>
    var cache: MutableList<IClientListener> =
        mutableListOf()//host这个进程A未注册到service，某些client要链接到A，因为A不存在，所以暂时缓存在这里

    fun registerClient(listener: IClientListener?) {
        listener?.let { client ->
            val locateFrom: String = client.locateFrom
            val linkToProcess: MutableList<out String> = client.linkToProcess
            if (linkToProcess.isNotEmpty()) {
                clients[locateFrom] = ClientBean(locateFrom, client)
            } else {
                cache.add(client)
            }

        }

    }

    fun unregisterClient(listener: IClientListener?) {
        listener?.let {
            val locateFrom: String = it.locateFrom
            val linkToProcess: MutableList<out String> = it.linkToProcess
            if (linkToProcess.isNotEmpty()) {
                clients.remove(locateFrom)
            } else {
                cache.remove(it)
            }
        }
    }

    fun registerHostListener(listener: IAppLocalInterface?) {
        listener?.let {
            val locateFrom: String = it.locateFrom
            if (!roots.containsKey(locateFrom)) {
                roots[locateFrom] = HostTab(locateFrom, it)
            }
        }
    }

    fun unregisterHostListener(listener: IAppLocalInterface?) {
        listener?.let {
            val locateFrom: String = it.locateFrom
            roots.remove(locateFrom)
        }
    }

    fun connectToChannel(connectInfo: ChannelConnectInfo?): ConnectResult {
        connectInfo?.run {
            val channelName: String = channelName
            val host = roots[hostFrom]
            return if (host == null) {
                ConnectResult(ResultCode.connectFailed, "host不存在或未连接此service")
            } else {
                val client = clients[clientFrom]
                if (client != null) {
                    host.channelNodeMap.add(channelName, client.uuid)
                    ConnectResult(ResultCode.success)
                } else {
                    ConnectResult(ResultCode.connectFailed, "client不存在或未连接此service")
                }

            }
        }
        return ConnectResult(ResultCode.connectFailed, "ChannelConnectInfo为null")
    }

    fun unConnectToChannel(connectInfo: ChannelConnectInfo?) {
        connectInfo?.run {
            val channelName: String = channelName
            val host = roots[hostFrom]
            if (host != null) {
                val client = clients[clientFrom]
                if (client != null) {
                    host.channelNodeMap.remove(channelName, client.uuid)
                }
            }
        }
    }

    /**
     * 将数据派发到client
     * 分发到通道,所有监听此通道的client都能接收到消息
     */
    fun dispatchDataToClient(connectInfo: Request) {
        val channelName = connectInfo.channelName
        roots[connectInfo.dataFrom]?.channelNodeMap?.getValuesList(channelName)?.forEach { uuid_ ->
            clients.filterValues {
                it.uuid == uuid_
            }.values.forEach {
                it.callBack.notifyDataChanged(convertToMessage(connectInfo))
            }
        }
    }
    fun dispatchDataToClient(connectInfo: EventMessage) {
        val channelName = connectInfo.channelName
        roots[connectInfo.dataFrom]?.channelNodeMap?.getValuesList(channelName)?.forEach { uuid_ ->
            clients.filterValues {
                it.uuid == uuid_
            }.values.forEach {
                it.callBack.notifyDataChanged(connectInfo)
            }
        }
    }

    /**
     * 将数据派发到client
     * 分发到指定的client
     */
    fun sendDataToClient(connectInfo: Request) {
        val clientName: String = connectInfo.dataTo
        clients[clientName]?.callBack?.notifyDataChanged(convertToMessage(connectInfo))
    }
    /**
     * 将数据派发到client
     * 分发到指定的client
     */
    fun sendDataToClient(connectInfo: EventMessage) {
        val clientName: String = connectInfo.dataTo
        clients[clientName]?.callBack?.notifyDataChanged(connectInfo)
    }

    private fun convertToMessage(info: Request): EventMessage {
       return EventMessage(info.dataFrom,info.dataTo,info.channelName,info.connectService,info.dataType,info.json)
    }

    /**
     * 发送数据到host
     */
    fun sendDataToHostChannel(request: Request) {
        roots[request.dataTo]?.root?.sendDataToHost(request)
    }

    fun sendMesToClient2(connectInfo: ChannelConnectInfo?) {
        connectInfo?.let {
            getMessage(it)
        }
    }

    /**
     * 从服务端特定channel中拿取最新消息
     */
    private fun getMessage(connectInfo: ChannelConnectInfo?) {
        if (connectInfo==null){
            Logs.d(TAG,"connectInfo为null")
        }else {
            val channelName: String = connectInfo.channelName
            val hostProcessName = connectInfo.hostFrom
             roots[hostProcessName]?.root?.getDataOnce(
                  Request(
                      connectInfo.hostFrom,
                      connectInfo.clientFrom,
                      connectInfo.channelName
                  )
              )
        }
    }

    companion object {
        @JvmStatic
        val INSTANCE: HostClientManager by lazy { HostClientManager() }
    }

}

class HostTab(
    val hostProcessName: String,//host进程名称
    val root: IAppLocalInterface//host的callBack
) {
    //<此host中的channel名称，监听此channel的client>
    //var channelNodeMap: MutableMap<String, MutableList<UUID>> = mutableMapOf()
    var channelNodeMap: ListTab<String, UUID> = ListTab()
}

class ClientBean(
    val processName: String,//client进程名称
    val callBack: IClientListener//client 的回调接口
) {
    val uuid: UUID = UUID.randomUUID()

    //此client监听的全部channel名称集合。<host进程名称,监听此host中的所有channel名称>
    //client连接到service后，client可能会监听不同的host中的channel，因此，以host的进程名称做区分
    var channelNameList: ListTab<String, String> = ListTab()
}