package com.kiylx.bus.eventbus.core

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import com.google.gson.Gson
import java.util.*

/**
 * 创建者 kiylx
 * 创建时间 2020/10/5 19:29
 * packageName：com.crystal.aplayer.module_base.tools.databus
 * 描述：存储消息通道，分发消息通道，全局配置调整.manager
 */
class MainBusManager private constructor() : LifecycleOwner {
    val mChannels //存放消息通道. Map<channelName,ChannelX<Object>>
            : MutableMap<String, ChannelX<*>>
    private val config //配置项
            : Config
    private var lifecycleObserverAlwaysActive: Boolean
    private var autoClear: Boolean
    val lifecycleRegistry: LifecycleRegistry
    override fun getLifecycle(): Lifecycle {
        return lifecycleRegistry
    }

    val gson: Gson by lazy { Gson() }

    var extention:Any?=null //用此实力扩展此类功能

    /**
     * @param <T>    消息通道的泛型类
     * @param target 消息通道名称
     * @param lifecycleOwner 生命周期，比如让channel跟随某个activity的lifecycle，destroy时不再发送消息。默认传入null，让channel跟随BusManager的生命周期
     * 控制消息通道的生命周期。null时，消息通道默认的生命周期是BusCore控制
     * @return 返回消息通道
    </T> */
    fun <T : Any> getChannel(target: String, lifecycleOwner: LifecycleOwner? = null, clazz: Class<T>): ChannelX<T> {
        if (!mChannels.containsKey(target)) {
            val channel = ChannelX<T>(target, clazz = clazz)
            if (lifecycleOwner == null)
                lifecycleRegistry.addObserver(channel)
            else
                lifecycleOwner.lifecycle.addObserver(channel)
            mChannels[target] = channel
        }
        return mChannels[target] as ChannelX<T>
    }

    /**
     * 远程调用时使用。
     *
     */
    fun getChannel2(target: String, lifecycleOwner: LifecycleOwner? = null): ChannelX<Any>? {
        val ch = mChannels[target]
        if (ch != null) {
            return if (ch.config().allowRemoteListen) {
                ch as ChannelX<Any>
            } else {
                null
            }
        }
        return null
    }

    /**
     * @param uuid 已存在的channel才会有uuid
     * @param <T>
     * @return
    </T> */
    fun <T : Any> getAfter(uuid: UUID?): ChannelX<T>? {
        for (c in mChannels.values) {
            if (c.uuid.compareTo(uuid) == 0) {
                return c as ChannelX<T>
            }
        }
        return null
    }

    fun <T : Any> getAfter(target: String, uuid: UUID?): ChannelX<T>? {
        for (c in mChannels.values) {
            if (c.uuid.compareTo(uuid) == 0 && c.channelName == target) {
                return c as ChannelX<T>
            }
        }
        return null
    }

    /**
     * 控制依附于BusCore的channel的生命周期
     */
    fun destroy() {
        lifecycleRegistry.markState(Lifecycle.State.DESTROYED)
        extention=null
    }

    fun config(): Config {
        return config
    }

    inner class Config {
        fun setLifecycleObserverAlwaysActive(b: Boolean): Config {
            lifecycleObserverAlwaysActive = b
            return this
        }

        fun setAutoClear(b: Boolean): Config {
            autoClear = b
            return this
        }
    }

    companion object {
        @JvmStatic
        val instance: MainBusManager
                by lazy(LazyThreadSafetyMode.SYNCHRONIZED) { MainBusManager() }
    }

    init {
        mChannels = HashMap()
        lifecycleObserverAlwaysActive = true
        autoClear = false
        config = Config()
        lifecycleRegistry = LifecycleRegistry(this)
        lifecycleRegistry.markState(Lifecycle.State.STARTED)
    }
}