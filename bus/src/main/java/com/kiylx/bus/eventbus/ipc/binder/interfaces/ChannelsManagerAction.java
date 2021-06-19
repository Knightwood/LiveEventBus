package com.kiylx.bus.eventbus.ipc.binder.interfaces;

import com.kiylx.bus.eventbus.ipc.binder.model.ChannelConnectInfo;
import com.kiylx.bus.eventbus.ipc.binder.model.EventMessage;

import org.jetbrains.annotations.NotNull;

public interface ChannelsManagerAction {
  void send(EventMessage data);

    void destroyChannel(@NotNull ChannelConnectInfo connectInfo);
}
