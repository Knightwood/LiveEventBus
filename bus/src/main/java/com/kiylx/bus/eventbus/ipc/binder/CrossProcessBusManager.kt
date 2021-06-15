package com.kiylx.bus.eventbus.ipc.binder

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import android.content.pm.PackageManager.NameNotFoundException
import android.text.TextUtils
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import com.kiylx.bus.eventbus.core.interfaces.BaseBusManager
import com.kiylx.bus.eventbus.ipc.binder.aidl.MessageService
import com.kiylx.bus.eventbus.ipc.binder.model.ServiceConnectInfo
import com.kiylx.bus.eventbus.utils.Logs
import java.util.*
import kotlin.collections.HashMap


/**
 * 创建者 kiylx
 * 创建时间 2021/6/11 12:29
 * 描述：存储消息通道，分发消息通道，全局配置调整.manager
 *消息源（服务端）->channel（连接到消息源，提供消息。不同channel提供不同消息）->observer（观察channel发布消息，observer会有多个，观察不同的channel）
 *
 * channel连接服务，观察者观察channel
 */
class CrossProcessBusManager private constructor() : BaseBusManager, LifecycleOwner {
    private val config: Config//配置项
    private val lifecycleRegistry: LifecycleRegistry
    private val channelsManagerMap: MutableMap<String, ChannelsManager> by lazy { mutableMapOf() }//<service全名,channelsManager>
    private val mContext: Context? = null

    /**
     * 控制消息通道的生命周期。null时，消息通道默认的生命周期是BusCore控制
     * @return 返回消息通道
     *
     * 查找channel,channel不存在，生成实例。channel存在，返回它
     */
    fun<T> getChannel(context: Context,connectInfo: ServiceConnectInfo): CrossChannel<T>? {
        val serviceName=(connectInfo.pkgName+connectInfo.clsName)
        if (channelsManagerMap.containsKey(serviceName))
            return channelsManagerMap[serviceName]?.getChannel<T>(connectInfo)
        else{
            val channelsManager=ChannelsManager()
            channelsManager.init(context,connectInfo)
            channelsManagerMap[serviceName] = channelsManager
            return channelsManager.getChannel(channelInfo = connectInfo)
        }
    }

    override fun getLifecycle(): Lifecycle {
        return lifecycleRegistry
    }

    /**
     * 控制依附于BusCore的channel的生命周期
     */
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

}