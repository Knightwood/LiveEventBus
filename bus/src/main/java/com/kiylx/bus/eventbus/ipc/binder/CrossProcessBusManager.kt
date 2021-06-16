package com.kiylx.bus.eventbus.ipc.binder

import android.content.Context
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import com.kiylx.bus.eventbus.core.interfaces.BaseBusManager
import com.kiylx.bus.eventbus.ipc.binder.interfaces.CrossProcessBusManagerAction
import com.kiylx.bus.eventbus.ipc.binder.model.ChannelsConnectInfo
import com.kiylx.bus.eventbus.ipc.binder.model.ServiceConnectInfo
import java.util.*


/**
 * 创建者 kiylx
 * 创建时间 2021/6/11 12:29
 * 描述：存储消息通道，分发消息通道，全局配置调整.manager
 * CrossProcessBusManager管理着连接到不同service的ChannelsManager
 */
class CrossProcessBusManager private constructor() : BaseBusManager, LifecycleOwner, CrossProcessBusManagerAction {
    private val config: Config//配置项
    private val lifecycleRegistry: LifecycleRegistry
    private val channelsManagerMap: MutableMap<String, ChannelsManager> by lazy { mutableMapOf() }//<service全名,channelsManager>
    private val mContext: Context? = null

    /**
     * 根据connectInfo查找ChannelsManager,ChannelsManager不存在就创建它,并通过ChannelsManager获得channel。
     * @return 返回消息通道
     *
     * 查找channel,channel不存在，生成实例。channel存在，返回它
     */
    fun <T> getChannel(context: Context, connectInfo: ChannelsConnectInfo): CrossChannel<T>? {
        val serviceName = (connectInfo.pkgName + connectInfo.clsName)
        if (channelsManagerMap.containsKey(serviceName))
            return channelsManagerMap[serviceName]?.getChannel<T>(connectInfo)
        else {
            val channelsManager = ChannelsManager(context, this@CrossProcessBusManager)
            channelsManager.initManager(context, connectInfo)
            channelsManagerMap[serviceName] = channelsManager
            return channelsManager.getChannel(channelInfo = connectInfo)
        }
    }

    override fun getLifecycle(): Lifecycle {
        return lifecycleRegistry
    }

    fun destroy() {
        lifecycleRegistry.markState(Lifecycle.State.DESTROYED)
    }

    fun config(): Config {
        return config
    }

    inner class Config {
        var lifecycleObserverAlwaysActive: Boolean = true
        var autoClear: Boolean = false

        fun setLifecycleObserverAlwaysActive(b: Boolean): Config {
            lifecycleObserverAlwaysActive = b
            return this
        }

        fun setAutoClear(b: Boolean): Config {
            autoClear = b
            return this
        }

        fun build(): CrossProcessBusManager {
            return this@CrossProcessBusManager
        }
    }

    init {
        config = Config()
        lifecycleRegistry = LifecycleRegistry(this)
        lifecycleRegistry.markState(Lifecycle.State.STARTED)
    }

    companion object {
        @JvmStatic
        val instance: CrossProcessBusManager
                by lazy(LazyThreadSafetyMode.SYNCHRONIZED) { CrossProcessBusManager() }
        const val tag = "跨进程BusManager"
    }

    /**
     * 删除不再使用的channelsManager
     */
    override fun deleteChannelsManager(info: ServiceConnectInfo) {
        val serviceName = (info.pkgName + info.clsName)
        channelsManagerMap.remove(serviceName)
    }

}