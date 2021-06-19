package com.kiylx.bus.eventbus.ipc.binder

import androidx.lifecycle.LifecycleOwner
import com.kiylx.bus.eventbus.core.OstensibleObserver
import com.kiylx.bus.eventbus.core.interfaces.BaseChannel
import com.kiylx.bus.eventbus.core.interfaces.Mode
import com.kiylx.bus.eventbus.ipc.binder.base.ObserverWrapper
import com.kiylx.bus.eventbus.ipc.binder.interfaces.ChannelAction
import com.kiylx.bus.eventbus.ipc.binder.interfaces.ChannelsManagerAction
import com.kiylx.bus.eventbus.ipc.binder.model.ChannelConnectInfo
import java.util.*

/**
 * 同一个channel中存储着所有关注同一个远程消息发布者(同一种消息或数据)的观察者。
 * 一个channel监听同一种消息或者说数据
 */
class CrossChannel<T>(channelsManagerAction: ChannelsManagerAction, channelInfo: ChannelConnectInfo) : BaseChannel(), ChannelAction {
    val tag = "跨进程Channel"
    private var connectInfo: ChannelConnectInfo = channelInfo //连接信息，连接到哪个服务
    private var observersMap: MutableMap<UUID, ObserverWrapper<*>> = mutableMapOf()//观察同一个消息源的观察者集合
    private var mChannelsManagerAction: ChannelsManagerAction? = channelsManagerAction
    private var locateData: T? = null//从service拿到的数据副本

    fun observe(lifecycleOwner: LifecycleOwner, ostensibleObserver: OstensibleObserver<T>): Unit {
        val observerWrapper = putIfAbsent(lifecycleOwner, ostensibleObserver)
        if (observerWrapper != null) {
            ostensibleObserver
                    .Config()
                    .setCrossProcess(Mode.binder)
                    .build()
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
     * 向服务端发送数据
     */
    fun <T> sendToRemote(data: T) {
        mChannelsManagerAction?.send(data)
    }

    /**
     * @param ostensibleObserver
     * @return 在map中能找到ostensibleObserver.uuid对应的ObserverAgent实例，返回null。
     * 在map中找不到ostensibleObserver.uuid对应的ObserverWrapper实例，放入new出来的值，并返回此实例。
     */
    private fun putIfAbsent(lifecycleOwner: LifecycleOwner, ostensibleObserver: OstensibleObserver<T>): ObserverWrapper<T>? {
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
        clear()
    }

    fun notifyObserver(data: T) {
        // 遍历map并发送数据
        locateData = data
        observersMap.values.forEach {
            (it as ObserverWrapper<T>).notify(data)
        }
    }


    private val config: Config by lazy { Config() }

    fun configChannel(): Config {
        return config
    }

    inner class Config {
        //可配置项
        private var procressLimit = false//进程隔离

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