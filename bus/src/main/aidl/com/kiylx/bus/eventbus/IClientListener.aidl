// IClientListener.aidl
package com.kiylx.bus.eventbus;

// Declare any non-default types here with import statements
import com.kiylx.bus.eventbus.ipc.binder.model.EventMessage;

interface IClientListener {
   void notifyDataChanged(out EventMessage message);
}