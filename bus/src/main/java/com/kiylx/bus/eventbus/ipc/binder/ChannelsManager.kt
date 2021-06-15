package com.kiylx.bus.eventbus.ipc.binder

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.IBinder
import android.os.RemoteException
import android.text.TextUtils
import com.kiylx.bus.eventbus.IClientListener
import com.kiylx.bus.eventbus.IMessageManager
import com.kiylx.bus.eventbus.ipc.binder.aidl.MessageService
import com.kiylx.bus.eventbus.ipc.binder.interfaces.ChannelsManagerAction
import com.kiylx.bus.eventbus.ipc.binder.model.ServiceConnectInfo
import com.kiylx.bus.eventbus.utils.Logs
import java.util.*

/**
 * 一伙channel连接service1，另一伙channel连接service2.
 * 因此
 * ChannelsManager持有一伙channel,不同的ChannelsManager连接到不同的service。
 */
class ChannelsManager : ChannelsManagerAction {
    private var connectInfo: ServiceConnectInfo? = null//连接信息，连接到哪个服务
    var uuid: UUID = UUID.randomUUID()
        private set
    var mIsBound: Boolean = false

    //存储着所有消息通道，不同消息通道可能会连接不同的服务端<channelName,crossChannel>
    private val channelList: MutableMap<String, CrossChannel<Any?>> by lazy { mutableMapOf() }
    private var mContext: Context? = null

    var mProcessManager: IMessageManager? = null
    var mProcessCallback = object : IClientListener.Stub() {
        override fun notifyMessage() {
            TODO("根据参数发送数据给channel")
            //channelList["demo"]?.notifyObserver(data)
        }

    }

    fun init(context: Context, connectInfo: ServiceConnectInfo) {
        bindService(context, connectInfo)
    }

    fun <T> getChannel(channelInfo: ServiceConnectInfo): CrossChannel<T> {
        var channel: CrossChannel<Any?>? = connectInfo?.let { channelList.getValue(it.channelName) }
        if (channel == null) {
            channel = CrossChannel(this@ChannelsManager)
        }
        return channel as CrossChannel<T>
    }

    private var mServiceConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            mProcessManager = IMessageManager.Stub.asInterface(service)
            if (mProcessManager == null) return
            try {
                mProcessManager!!.registerListener(mProcessCallback)
            } catch (e: RemoteException) {
                e.printStackTrace()
            }
        }

        override fun onServiceDisconnected(name: ComponentName) {
            mProcessManager = null
            Logs.d(CrossProcessBusManager.tag, "onServiceDisconnected, process = $name")
        }
    }

    private fun bindService(context: Context, connectInfo: ServiceConnectInfo) {
        //连接到服务端
        val intent = Intent(Intent.ACTION_MAIN)
        intent.component = ComponentName(connectInfo.pkgName, connectInfo.clsName)
        mIsBound = context.bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE)
        this.mIsBound = mIsBound
        if (!mIsBound) {
            Logs.e(CrossChannel.tag, "Can not find the host app under :${connectInfo.pkgName}")
            if (Logs.DEBUG >= Logs.nowLevel) {
                throw RuntimeException("Can not find the host app under :" + connectInfo.pkgName)
            }
        }
    }


    private fun unbindService() {
        if (mIsBound) {
            mContext?.unbindService(mServiceConnection)
            if (mProcessManager != null && mProcessManager!!.asBinder().isBinderAlive()) {
                try {
                    // 取消注册
                    mProcessManager!!.unregisterListener(mProcessCallback)
                } catch (e: RemoteException) {
                    e.printStackTrace()
                }
            }
            mIsBound = false
            mContext = null

        }

    }

    override fun <T : Any?> send(data: T) {
        TODO("Not yet implemented")
    }

    companion object {
        const val tag = "ChannelsManager"
    }
}