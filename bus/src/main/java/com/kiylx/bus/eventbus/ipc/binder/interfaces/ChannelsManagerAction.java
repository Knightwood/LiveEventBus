package com.kiylx.bus.eventbus.ipc.binder.interfaces;

import com.kiylx.bus.eventbus.ipc.binder.model.ChannelsConnectInfo;

import org.jetbrains.annotations.NotNull;

public interface ChannelsManagerAction {
    <T> void send(T data);

    void destroyChannel(@NotNull ChannelsConnectInfo connectInfo);
}
