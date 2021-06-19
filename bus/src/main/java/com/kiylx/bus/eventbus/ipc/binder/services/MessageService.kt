package com.kiylx.bus.eventbus.ipc.binder.services

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.kiylx.bus.eventbus.IClientListener
import com.kiylx.bus.eventbus.IMessageManager
import com.kiylx.bus.eventbus.ipc.binder.model.EventMessage
import com.kiylx.bus.eventbus.ipc.binder.model.ChannelConnectInfo
import com.kiylx.bus.eventbus.ipc.binder.model.ConnectResult

/**
 * 进程创建时调用，一般在 Application 的 onCreate 中调用
 * 单应用多进程场景请使用{BUS_SUPPORT_MULTI_APP : false}
 * <p>
 * 多应用且多进程场景请使用{BUS_SUPPORT_MULTI_APP : true}
 * 同时配置应用包名 {BUS_MAIN_APPLICATION_ID :共享服务且常驻的包名 }
 * 主应用必须安装，否则不能正常运行
 * <p>
 * eg:<pre><code>{
 *      manifestPlaceholders = [
 *         BUS_SUPPORT_MULTI_APP  : true,
 *         BUS_MAIN_APPLICATION_ID: "com.example.bus"
 *     ]
 * }</code></pre>
 */

class MessageService() : Service() {
    private var mProcessManager:ProcessManager= ProcessManager.instance

    private var mBinder = object : IMessageManager.Stub() {
        override fun registerListener(listener: IClientListener?) {
            if (listener != null) {
                mProcessManager.registerLocate(listener)
            }
        }

        override fun unregisterListener(listener: IClientListener?) {
            if (listener != null)
                mProcessManager.unregisterLocate(listener)
        }

        /**
         * 发送数据到服务端
         */
        override fun sendMessage(message: EventMessage?) {
            mProcessManager.dispatchMsgToChannel(message)
        }

        /**
         * 删除服务端的一个observer
         */
        override fun deleteObserver(connectInfo: ChannelConnectInfo?) {
            mProcessManager.unListenChannel(connectInfo)
        }

        /**
         * 添加一个监听某个channel的observer到服务端
         */
        override fun requestConnect(connectInfo: ChannelConnectInfo?): ConnectResult {
          return  mProcessManager.listenChannel(connectInfo)
        }

        /**
         * 从服务端的某个channel中拿一次数据
         */
        override fun getMessageOnces(connectInfo: ChannelConnectInfo?): EventMessage {
           return mProcessManager.getMessage(connectInfo)
        }

    }

    override fun onCreate() {
        super.onCreate()

    }

    override fun onDestroy() {
        super.onDestroy()
       mProcessManager.destroy()
    }

    override fun onBind(intent: Intent): IBinder {
        return mBinder
    }
}