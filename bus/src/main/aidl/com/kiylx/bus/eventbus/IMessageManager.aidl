// IMessageManager.aidl
package com.kiylx.bus.eventbus;

// Declare any non-default types here with import statements
import com.kiylx.bus.eventbus.IClientListener;
import com.kiylx.bus.eventbus.ipc.binder.model.EventMessage;
import com.kiylx.bus.eventbus.ipc.binder.model.ServiceConnectInfo;

interface IMessageManager {
    void registerListener(IClientListener listener); //注册接口
    void unregisterListener(IClientListener listener); //解注册接口
    void sendMessage(in EventMessage message);
    void deleteObserver(in ServiceConnectInfo connectInfo);
}