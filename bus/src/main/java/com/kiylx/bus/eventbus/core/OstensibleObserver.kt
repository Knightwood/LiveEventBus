package com.kiylx.bus.eventbus.core

import com.kiylx.bus.eventbus.core.interfaces.Mode
import java.util.*

/**
 * 创建者 kiylx
 * 创建时间 2021/1/2 13:17
 * packageName：com.kiylx.bus.eventbus.core
 * 描述：虚假的Observer,描述对于observer的配置信息，且有observer的功能，不是对于真正的observer的包装。
 */
abstract class OstensibleObserver<T> {
    val uuid: UUID = UUID.randomUUID()
    private val config: Config by lazy { Config() }

    /**
     * Called when the data is changed.
     *
     * @param t The new data
     */
    abstract fun onChanged(t: T)


    fun config(): Config {
        return config
    }

    inner class Config {
        var crossProcess = Mode.normal
        //观察者是否想接受消息
        var isWantAcceptMessage = true
        //是否开启粘性，true：开启
        var isSticky = true

        fun setSticky(b: Boolean): Config {
            isSticky = b
            return this
        }

        fun setWantAcceptMessage(b: Boolean): Config {
            isWantAcceptMessage = b
            return this
        }
        fun setCrossProcess(mode: Mode): Config {
            crossProcess=mode
            return this
        }

        fun build(): OstensibleObserver<T> {
            return this@OstensibleObserver
        }
    }
}