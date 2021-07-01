package com.kiylx.bus.eventbus.ipc.binder.services

import com.kiylx.bus.eventbus.IClientListener
import com.kiylx.bus.eventbus.core.OstensibleObserver

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