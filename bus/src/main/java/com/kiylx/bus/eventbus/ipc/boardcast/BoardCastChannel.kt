package com.kiylx.bus.eventbus.ipc.boardcast

import com.kiylx.bus.eventbus.core.interfaces.BaseChannel
import com.kiylx.bus.eventbus.core.interfaces.Mode

class BoardCastChannel private constructor() : BaseChannel() {


    private val config: Config by lazy { Config() }

    fun config(): Config {
        return config
    }

    inner class Config{
        //可配置项
        private var isCanPush = true //通道是否可以发送消息
        private var crossProcess = Mode.normal

        fun setCanPushMes(b: Boolean): Config {
            isCanPush = b
            return this
        }

        fun setIsUseCrossProcess(mode: Mode): Config {
            crossProcess = mode
            return this
        }

        fun build(): BoardCastChannel {
            return this@BoardCastChannel
        }
    }

    companion object {
        @JvmStatic
        val instance: BoardCastChannel by lazy(LazyThreadSafetyMode.SYNCHRONIZED) { BoardCastChannel() }
    }
}