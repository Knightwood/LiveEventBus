package com.kiylx.bus.eventbus.ipc.binder.interfaces;

public interface ChannelsManagerAction {
    <T> void send(T data);
}
