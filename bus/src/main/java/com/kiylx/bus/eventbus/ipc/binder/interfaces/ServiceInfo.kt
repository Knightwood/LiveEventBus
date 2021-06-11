package com.kiylx.bus.eventbus.ipc.binder.interfaces

import android.content.Context

/**
 * 描述客户端所要连接到的服务端的信息。
 */
data class ServiceInfo (val action: String, val pkg: Context, val cls: Class<*>)
