package com.kiylx.bus.eventbus.core

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import com.google.gson.Gson
import com.kiylx.bus.eventbus.core.interfaces.BaseBusManager
import java.util.*

/**
 * 创建者 kiylx
 * 创建时间 2020/10/5 19:29
 * packageName：com.crystal.aplayer.module_base.tools.databus
 * 描述：存储消息通道，分发消息通道，全局配置调整.manager
 */
class MainBusManager private constructor() :BaseBusManager,LifecycleOwner {
    private val mChannels //存放消息通道. Map<channelName,Channel<Object>>
            : MutableMap<String?, Channel<Any>>
    private val config //配置项
            : Config
    private var lifecycleObserverAlwaysActive: Boolean
    private var autoClear: Boolean
    private val lifecycleRegistry: LifecycleRegistry
    override fun getLifecycle(): Lifecycle {
        return lifecycleRegistry
    }
val gson:Gson by    lazy { Gson() }
    /**
     * @param <T>    消息通道的泛型类
     * @param target 消息通道名称
     * @param lifecycleOwner 生命周期，比如让channel跟随某个activity的lifecycle，destroy时不再发送消息。默认传入null，让channel跟随BusManager的生命周期
     * 控制消息通道的生命周期。null时，消息通道默认的生命周期是BusCore控制
     * @return 返回消息通道
    </T> */
    fun <T> getChannel(target: String, lifecycleOwner: LifecycleOwner?=null): Channel<T> {
        if (!mChannels.containsKey(target)) {
            val channel = Channel<Any>(target)
            if (lifecycleOwner == null)
                lifecycleRegistry.addObserver(channel)
            else
                lifecycleOwner.lifecycle.addObserver(channel)
            mChannels[target] = channel
        }
        return mChannels[target] as Channel<T>
    }

    fun getChannel2(target: String, lifecycleOwner: LifecycleOwner?=null): Channel<Any>? {
        if (!mChannels.containsKey(target)) {
           /* val channel = Channel<Any>(target)
            if (lifecycleOwner == null)
                lifecycleRegistry.addObserver(channel)
            else
                lifecycleOwner.lifecycle.addObserver(channel)
            mChannels[target] = channel*/
        return null
        }
        return mChannels[target] as Channel<Any>
    }

    /**
     * @param uuid 已存在的channel才会有uuid
     * @param <T>
     * @return
    </T> */
    fun <T> getAfter(uuid: UUID?): Channel<T>? {
        for (c in mChannels.values) {
            if (c.uuid.compareTo(uuid) == 0) {
                return c as Channel<T>
            }
        }
        return null
    }

    fun <T> getAfter(target: String, uuid: UUID?): Channel<T>? {
        for (c in mChannels.values) {
            if (c.uuid.compareTo(uuid) == 0 && c.channelName == target) {
                return c as Channel<T>
            }
        }
        return null
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