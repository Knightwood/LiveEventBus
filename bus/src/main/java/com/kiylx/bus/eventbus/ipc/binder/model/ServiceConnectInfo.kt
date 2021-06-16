package com.kiylx.bus.eventbus.ipc.binder.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

/**
 * 描述客户端所要连接到的服务端的信息。
 * @param pkgName service的包名
 * @param clsName service类名
 * @param isBound service是否连接
 */
@Parcelize
data class ServiceConnectInfo(val pkgName: String,
                              val clsName: String,
                              var isBound: Boolean = false) : Parcelable
