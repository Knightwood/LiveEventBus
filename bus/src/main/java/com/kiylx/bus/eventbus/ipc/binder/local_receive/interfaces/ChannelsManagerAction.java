package com.kiylx.bus.eventbus.ipc.binder.local_receive.interfaces;

import com.kiylx.bus.eventbus.ipc.binder.model.ChannelConnectInfo;
import com.kiylx.bus.eventbus.ipc.binder.model.EventMessage;
import com.kiylx.bus.eventbus.ipc.binder.model.Request;

import org.jetbrains.annotations.NotNull;

public interface ChannelsManagerAction {
  void send(Request data);

    void destroyChannel(@NotNull ChannelConnectInfo connectInfo);
}
