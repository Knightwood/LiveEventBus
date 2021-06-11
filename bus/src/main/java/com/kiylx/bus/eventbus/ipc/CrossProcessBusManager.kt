package com.kiylx.bus.eventbus.ipc

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import com.kiylx.bus.eventbus.core.interfaces.BaseBusManager
import com.kiylx.bus.eventbus.ipc.binder.BinderChannel
import com.kiylx.bus.eventbus.ipc.binder.interfaces.ServiceInfo
import com.kiylx.bus.eventbus.ipc.boardcast.BoardCastChannel
import java.util.*

/**
 * 创建者 kiylx
 * 创建时间 2021/6/11 12:29
 * 描述：存储消息通道，分发消息通道，全局配置调整.manager
 */
class CrossProcessBusManager private constructor() : BaseBusManager, LifecycleOwner {
    private val config: Config//配置项
    private val lifecycleRegistry: LifecycleRegistry

    /**
     * @param lifecycleOwner 生命周期，比如让channel跟随某个activity的lifecycle，destroy时不再发送消息。
     * 控制消息通道的生命周期。null时，消息通道默认的生命周期是BusCore控制
     * @return 返回消息通道
     *
     * binder
     */
    fun getChannel(info: ServiceInfo, lifecycleOwner: LifecycleOwner? = null): BinderChannel {
        return BinderChannel.instance
    }

    /**
     * 广播
     */
    fun getChannel(): BoardCastChannel {
        return BoardCastChannel.instance
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

    companion object {
        @JvmStatic
        val instance: CrossProcessBusManager
                by lazy(LazyThreadSafetyMode.SYNCHRONIZED) { CrossProcessBusManager() }
    }

    init {
        config = Config()
        lifecycleRegistry = LifecycleRegistry(this)
        lifecycleRegistry.markState(Lifecycle.State.STARTED)
    }
}