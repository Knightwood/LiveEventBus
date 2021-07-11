package com.kiylx.bus.eventbus.ipc.binder.local_receive

import androidx.lifecycle.LifecycleOwner
import com.kiylx.bus.eventbus.core.OstensibleObserver
import com.kiylx.bus.eventbus.core.interfaces.Cornerstone2
import com.kiylx.bus.eventbus.ipc.binder.local_receive.base.ObserverWrapper
import com.kiylx.bus.eventbus.ipc.binder.local_receive.interfaces.ChannelAction
import com.kiylx.bus.eventbus.ipc.binder.local_receive.interfaces.ChannelsManagerAction
import com.kiylx.bus.eventbus.ipc.binder.model.ChannelConnectInfo
import com.kiylx.bus.eventbus.ipc.binder.model.EventMessage
import com.kiylx.bus.eventbus.ipc.binder.model.Request
import com.kiylx.bus.eventbus.ipc.binder.util.currentProcessName
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import java.util.*

/**
 * 同一个channel中存储着所有关注同一个远程消息发布者(同一种消息或数据)的观察者。
 * 一个channel监听同一种消息或者说数据
 */
class CrossChannel<T : Any>(
    channelsManagerAction: ChannelsManagerAction,
    channelInfo: ChannelConnectInfo,
    val clazz: Class<T>
) : Cornerstone2(), ChannelAction {
    val tag = "跨进程Channel"
    private var connectInfo: ChannelConnectInfo = channelInfo //连接信息，连接到哪个服务
    private var observersMap: MutableMap<UUID, ObserverWrapper<*>> = mutableMapOf()//观察同一个消息源的观察者集合
    private var mChannelsManagerAction: ChannelsManagerAction? = channelsManagerAction
    private val mConfig: Config by lazy { Config() }

    private var locateData: T? = null//从service拿到的数据副本
    private val toRemote: Channel<Request> = Channel(mConfig.remoteLimit)//向服务端发送
    private val toLocal: Channel<EventMessage> = Channel(mConfig.localLimit)//从服务端接受

    init {
        launch(coroutineContext) {
            while (true) {
                val data = toRemote.receive()
                mChannelsManagerAction?.send(data)
            }
        }
        launch(coroutineContext) {
            while (true) {
                val data = toLocal.receive()
                locateData = convertFromMessage(data)
                observersMap.values.forEach {
                    (it as ObserverWrapper<T>).notify(locateData!!)
                }
            }
        }
    }


    /**
     * 向服务端发送数据
     */
    fun sendToRemote(data: T, dataTo: String) {
        launch {
            val mes = Request(
                currentProcessName,
                dataTo,
                connectInfo.channelName,
                connectInfo.pkgName+connectInfo.clsName,
                "",
                covertToJson(data)
            )
            toRemote.send(mes)
        }
    }

    fun observe(lifecycleOwner: LifecycleOwner, ostensibleObserver: OstensibleObserver<T>): Unit {
        val observerWrapper = putIfAbsent(lifecycleOwner, ostensibleObserver)
        if (observerWrapper != null) {
            if (observerWrapper.observer.config().isSticky && observerWrapper.firstNotify) {
                if (locateData != null) {
                    val tmp = locateData
                    tmp?.let { observerWrapper.notify(it) }
                    observerWrapper.firstNotify = false
                }

            }
        }
    }

    /**
     * @param ostensibleObserver
     * @return 在map中能找到ostensibleObserver.uuid对应的ObserverAgent实例，返回null。
     * 在map中找不到ostensibleObserver.uuid对应的ObserverWrapper实例，放入new出来的值，并返回此实例。
     */
    private fun putIfAbsent(
        lifecycleOwner: LifecycleOwner,
        ostensibleObserver: OstensibleObserver<T>
    ): ObserverWrapper<T>? {
        var v = observersMap[ostensibleObserver.uuid]
        if (v == null) {
            v = ObserverWrapper(ostensibleObserver, lifecycleOwner, this@CrossChannel)
            observersMap[ostensibleObserver.uuid] = v
            return v
        }
        return null
    }

    /**
     * 不再有本地的观察者观察channel中的数据或消息，删除本地observer,
     * 如果channel不再有任何一个observer，请求上层删除本地channel以及service端channel的连接
     */
    override fun destroyObserver(uuid: UUID) {
        synchronized(observersMap) {
            observersMap.remove(uuid)//移除本地observer
            if (observersMap.isEmpty()) {
                mChannelsManagerAction?.destroyChannel(connectInfo)
                destroySelf()
            }
        }
    }

    private fun destroySelf() {
        locateData = null
        mChannelsManagerAction = null
        toLocal.close()
        toRemote.close()
        clear()
    }

    fun notifyObserver(data: EventMessage) {
        launch {
            toLocal.send(data)
        }
    }

    suspend fun covertToJson(t: T): String = withContext(Dispatchers.Default) {
        CrossProcessBusManager.instance.gson.toJson(t)
    }

    suspend fun convertFromMessage(message: EventMessage?): T? = withContext(Dispatchers.Default) {
        if (message != null) {
            CrossProcessBusManager.instance.gson.fromJson(message.json, clazz)
        } else {
            null
        }
    }

    fun configChannel(): Config {
        return mConfig
    }

    inner class Config {
        //可配置项
        private var procressLimit = false//进程隔离
        var localLimit: Int = 10
        var remoteLimit: Int = 10

        /**
         * 设置从远程端接受消息后，消息队列的缓存容量
         */
        fun setLocalLimit(limit: Int): Config {
            localLimit = limit
            return this
        }

        /**
         * 设置发送到远程端消息的消息队列缓存容量
         */
        fun setRemoteLimit(limit: Int): Config {
            remoteLimit = limit
            return this
        }

        fun setConnectInfo(connInfo: ChannelConnectInfo): Config {
            connectInfo = connInfo
            return this
        }

        fun build(): CrossChannel<T> {
            return this@CrossChannel
        }
    }

    companion object {
        const val tag = "跨进程Channel"
    }


}