// IClientListener.aidl
package com.kiylx.bus.eventbus;

// Declare any non-default types here with import statements
import com.kiylx.bus.eventbus.ipc.binder.model.EventMessage;

interface IClientListener {
    //将数据从服务端传到客户端
   void notifyDataChanged(out EventMessage message);
   //给予服务端客服端的来源信息
   String getLocateFrom();
 }