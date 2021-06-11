package com.kiylx.bus.eventbus.ipc.binder

import android.content.ComponentName
import android.content.Intent
import com.kiylx.bus.eventbus.core.OstensibleObserver
import com.kiylx.bus.eventbus.core.interfaces.BaseChannel
import com.kiylx.bus.eventbus.core.interfaces.Mode
import com.kiylx.bus.eventbus.ipc.binder.interfaces.ServiceInfo

class BinderChannel private constructor(): BaseChannel() {//, Action


    fun observeCrossProcess(ostensibleObserver: OstensibleObserver<T>, info: ServiceInfo) {
        observerCrossProcessInternal(ostensibleObserver
                .Config()
                .setCrossProcess(Mode.binder)
                .build(), info)
    }

    private fun observerCrossProcessInternal(ostensibleObserver: OstensibleObserver<T>, info: ServiceInfo) {

        val existing = getObserverAgent(ostensibleObserver)
        if (existing != null) {//连接到服务端
            val intent = Intent(info.action)
            intent.component = ComponentName(info.pkg, info.cls)

        }

    }


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

        fun build(): BinderChannel {
            return this@BinderChannel
        }
    }
    companion object{
        @JvmStatic
        val instance:BinderChannel by lazy(LazyThreadSafetyMode.SYNCHRONIZED) { BinderChannel() }
    }
}