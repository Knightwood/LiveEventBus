package com.kiylx.bus.eventbus.ipc.binder.local_receive

import android.content.Context
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import com.google.gson.Gson
import com.kiylx.bus.eventbus.ipc.binder.local_receive.interfaces.CrossProcessBusManagerAction
import com.kiylx.bus.eventbus.ipc.binder.model.ChannelConnectInfo
import java.util.*


/**
 * 创建者 kiylx
 * 创建时间 2021/6/11 12:29
 * 描述：存储消息通道，分发消息通道，全局配置调整.manager
 * CrossProcessBusManager管理着连接到不同service的ChannelsManager
 */
class CrossProcessBusManager private constructor() :  LifecycleOwner, CrossProcessBusManagerAction {
    private val config: Config//配置项
    private val lifecycleRegistry: LifecycleRegistry
    private val channelsManagerMap: MutableMap<String, ChannelsManager> by lazy { mutableMapOf() }//<service全名,channelsManager>
    private val mContext: Context? = null
    val gson: Gson by lazy { Gson() }

    /**
     * 根据connectInfo查找ChannelsManager,ChannelsManager不存在就创建并初始化,并通过ChannelsManager获得channel。
     * @return 返回消息通道
     *
     * 查找channel,channel不存在，生成实例。channel存在，返回它
     */
    fun <T : Any> getChannel(context: Context, connectInfo: ChannelConnectInfo, clazz: Class<T>): CrossChannel<T>? {
        val serviceName = (connectInfo.pkgName + connectInfo.clsName)
        //ChannelsManager存在，获得channel。不存在，new出来初始化并获得channel
        return if (channelsManagerMap.containsKey(serviceName))
            channelsManagerMap[serviceName]?.getChannel<T>(channelInfo = connectInfo, clazz)
        else {
            val channelsManager = ChannelsManager(context, this@CrossProcessBusManager)
            channelsManager.initManager(context, connectInfo)
            channelsManagerMap[serviceName] = channelsManager
            channelsManager.getChannel(channelInfo = connectInfo, clazz)
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
    override fun deleteChannelsManager(info: String) {
        channelsManagerMap.remove(info)
    }

}