package com.kiylx.bus.eventbus.ipc.binder

import android.app.ActivityManager
import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import android.os.Process
import android.text.TextUtils
import com.kiylx.bus.eventbus.ipc.binder.model.ChannelConnectInfo
import com.kiylx.bus.eventbus.ipc.binder.services.MessageService
import com.kiylx.bus.eventbus.utils.Logs


fun getServiceName(channelConnectInfo: ChannelConnectInfo): String {
    return channelConnectInfo.pkgName + channelConnectInfo.clsName
}

fun getServiceName(context: Context): String? {
    var mPkgName: String? = null
    try {
        val cn = ComponentName(context, MessageService::class.java)
        val info = context.packageManager.getServiceInfo(cn, PackageManager.GET_META_DATA)
        val supportMultiApp = info.metaData.getBoolean("BUS_SUPPORT_MULTI_APP", false)
        if (!supportMultiApp) {
            mPkgName = context.packageName
        } else {
            val mainApplicationId = info.metaData.getString("BUS_MAIN_APPLICATION_ID")
            if (TextUtils.isEmpty(mainApplicationId)) {
                Logs.e(ChannelsManager.tag, "Can not find the host app under :${mPkgName}")
                if (Logs.DEBUG >= Logs.nowLevel) {
                    throw RuntimeException("Must config {BUS_MAIN_APPLICATION_ID} in manifestPlaceholders .")
                }
                return null
            } else {
                mPkgName = mainApplicationId
            }
        }

    } catch (e: PackageManager.NameNotFoundException) {
        e.printStackTrace()
    }
    return mPkgName
}

fun getProcessName(cxt: Context): String {
    val pid = Process.myPid()
    val am = cxt.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    val runningApps = am.runningAppProcesses ?: return "null"
    for (procInfo in runningApps) {
        if (procInfo.pid == pid) {
            return procInfo.processName
        }
    }
    return "null"
}