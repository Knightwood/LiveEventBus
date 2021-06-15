// IMessageManager.aidl
package com.kiylx.bus.eventbus;

// Declare any non-default types here with import statements
import com.kiylx.bus.eventbus.IClientListener;

interface IMessageManager {
    void registerListener(in IClientListener listener); //注册接口
    void unregisterListener(in IClientListener listener); //解注册接口
    void sendMessage();
    void deleteObserver();
}