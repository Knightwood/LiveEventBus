package com.kiylx.bus.eventbus.ipc.binder.interfaces

import java.util.*

interface ChannelAction {
    fun destroyObserver(uuid: UUID)
}