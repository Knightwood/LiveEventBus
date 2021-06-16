package com.kiylx.bus.eventbus.ipc.binder.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ConnectResult(var code: Int, var msg: String) : Parcelable

class ResultCode {
    companion object {
        const val success: Int = 200
        const val serviceNotFound = 300
        const val channelNotFound = 301
        const val convertError = 302
    }
}
