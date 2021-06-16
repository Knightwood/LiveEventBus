package com.kiylx.bus.eventbus.ipc.binder.interfaces

import com.kiylx.bus.eventbus.ipc.binder.model.ServiceConnectInfo

interface CrossProcessBusManagerAction {
    fun deleteChannelsManager(info: ServiceConnectInfo)
}