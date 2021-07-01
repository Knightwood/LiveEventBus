package com.kiylx.bus.eventbus.ipc.binder.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

/**
 *
 */
@Parcelize
data class Request(val dataFrom: String,
                   val dataTo: String,
                   val channelName: String,
                   val connectService: String,
                   val dataType: String
): Parcelable


