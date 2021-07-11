package com.kiylx.bus.eventbus.core.interfaces

/**
 * 预留接口，实现ipc使用
 */
interface CrossProcessAction {

    fun destroySelf(): Unit

    suspend fun <T> postDataToService(
        value: String,
        dataFrom: String,
        dataTo: String,
        thisChannelName: String,
        connectService: String,
        dataType: String
    ): Unit
}