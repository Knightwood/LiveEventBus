package com.kiylx.bus.eventbus.ipc.binder.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

/**
 * 描述客户端所要连接到的服务端的信息。
 * @param pkgName service的包名
 * @param clsName service类名
 * @param channelName 要监听的通道
 */
@Parcelize
data class ChannelConnectInfo(val pkgName: String,
                              val clsName: String,
                              val channelName: String,
                              val locateFrom: String
) : Parcelable


