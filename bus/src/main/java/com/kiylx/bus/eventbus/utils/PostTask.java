package com.kiylx.bus.eventbus.utils;

import androidx.lifecycle.LifecycleOwner;

/**
 * 创建者 kiylx
 * 创建时间 2021/1/1 20:44
 * packageName：com.kiylx.bus.eventbus.utils
 * 描述：给handler的runnable的类,接受Method实例和用于Method执行的参数
 */
public class PostTask implements Runnable {
    /**
     * newValue[0]:message
     * newValue[1]:LifecycleOwner,也就是sender
     */
    private Object[] newValue;
    private Method method;

    public PostTask(Method method, Object... newValue) {
        this.newValue = newValue;
        this.method = method;
    }

    @Override
    public void run() {
        method.method(newValue);
    }

    public <T> Runnable obtainMessage(Method mPostMethod, T message, LifecycleOwner sender) {
        this.method=mPostMethod;
        newValue= new Object[]{message, sender};
        return this;
    }

    public <T> Runnable obtainMessage(T message, LifecycleOwner sender) {
        newValue= new Object[]{message, sender};
        return this;
    }

    public <T> Runnable obtainNewMessage(Method mPostMethod,T message, LifecycleOwner sender) {
        return new PostTask(mPostMethod,message,sender);
    }

    public interface Method {
        void method(Object[] args);
    }
}
