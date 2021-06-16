// IMessageManager.aidl
package com.kiylx.bus.eventbus;

// Declare any non-default types here with import statements
import com.kiylx.bus.eventbus.IClientListener;
import com.kiylx.bus.eventbus.ipc.binder.model.EventMessage;
import com.kiylx.bus.eventbus.ipc.binder.model.ChannelsConnectInfo;
import com.kiylx.bus.eventbus.ipc.binder.model.ConnectResult;

interface IMessageManager {
    void registerListener(IClientListener listener); //注册接口
    void unregisterListener(IClientListener listener); //解注册接口
    void sendMessage(in EventMessage message);
    void deleteObserver(in ChannelsConnectInfo connectInfo);
    ConnectResult requestConnect(in ChannelsConnectInfo connectInfo);
    EventMessage getMessageOnces(in ChannelsConnectInfo connectInfo);
}