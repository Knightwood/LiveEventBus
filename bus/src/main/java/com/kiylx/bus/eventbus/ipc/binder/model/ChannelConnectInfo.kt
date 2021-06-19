package com.kiylx.bus.eventbus.ipc.binder.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

/**
 * 描述客户端所要连接到的服务端的信息。
 * @param pkgName 服务端service的包名
 * @param clsName 服务端service类名
 * @param channelName 要监听的通道
 * @param locateFrom 客户端来自哪个进程,进程名称
 */
@Parcelize
data class ChannelConnectInfo(val pkgName: String,
                              val clsName: String,
                              val channelName: String,
                              val locateFrom: String
) : Parcelable


