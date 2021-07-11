package com.kiylx.bus.eventbus.ipc.binder.local_receive.interfaces

import java.util.*

interface ChannelAction {
    fun destroyObserver(uuid: UUID)
}