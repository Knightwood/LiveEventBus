package com.kiylx.bus.eventbus.ipc.binder

import androidx.lifecycle.LifecycleOwner
import com.kiylx.bus.eventbus.core.OstensibleObserver
import com.kiylx.bus.eventbus.core.interfaces.BaseChannel
import com.kiylx.bus.eventbus.core.interfaces.Mode
import com.kiylx.bus.eventbus.ipc.binder.base.ObserverWrapper
import com.kiylx.bus.eventbus.ipc.binder.interfaces.ChannelAction
import com.kiylx.bus.eventbus.ipc.binder.interfaces.ChannelsManagerAction
import com.kiylx.bus.eventbus.ipc.binder.model.ServiceConnectInfo
import java.util.*

/**
 * 同一个channel中存储着所有关注同一个远程消息发布者的观察者。
 */
class CrossChannel<T>(private var channelsManagerAction: ChannelsManagerAction? = null) : BaseChannel(), ChannelAction {
    val tag = "跨进程Channel"
    private var connectInfo: ServiceConnectInfo? = null//连接信息，连接到哪个服务
    private var observersMap: MutableMap<UUID, ObserverWrapper<*>> = mutableMapOf()//观察同一个消息源的观察者集合

    fun observe(lifecycleOwner: LifecycleOwner, ostensibleObserver: OstensibleObserver<T>):Unit {
        val observerWrapper = putIfAbsent(lifecycleOwner,ostensibleObserver)
        if (observerWrapper != null) {
            ostensibleObserver
                    .Config()
                    .setCrossProcess(Mode.binder)
                    .build()
        }

    }

    /**
     * 向服务端发送数据
     */
    fun <T> send(data: T) {
        channelsManagerAction?.send(data)
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


    private fun deleteObserver() {

    }

    fun notifyObserver(message: T) {
        // 遍历数组并发送数据
        observersMap.values.forEach {
            (it.observer as OstensibleObserver<T>).onChanged(message)
        }
    }


    private val config: Config by lazy { Config() }

    fun configChannel(): Config {
        return config
    }

    inner class Config {
        //可配置项
        private var procressLimit = false//进程隔离

        fun setConnectInfo(connInfo: ServiceConnectInfo): Config {
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


    override fun destroyObserver() {
        TODO("Not yet implemented")
    }
}