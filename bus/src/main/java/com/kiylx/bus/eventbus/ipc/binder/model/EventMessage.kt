package com.kiylx.bus.eventbus.ipc.binder.model

import android.os.Parcel
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * 进程间缓存事件封装类，事件定义
 *@param dataFrom 来自哪个client进程
 *@param dataTo 去往哪个host进程
 *@param channelName 要监听的channel名称
 *@param connectService 连接到的服务。 service与host进程不是同一个进程。所以有dataTo和connectService的区别
 *@param dataType
 */
@Parcelize
data class EventMessage(
    val dataFrom: String="",
    val dataTo: String="",
    val channelName: String="",
    val connectService: String="",
    val dataType: String="",
    val json: String =""
) : Parcelable {
    fun readFromParcel(_reply: Parcel) {

    }
}


