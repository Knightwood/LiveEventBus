package com.kiylx.bus.eventbus.ipc.binder.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 进程间缓存事件封装类，事件定义
 */
public class EventMessage implements Parcelable {
    // 发送事件所在进程
    String processName;
    // 发送事件到某个channel
    String channel;
    // 发送的事件名
    String event;
    // 发送的事件类型
    String type;
    // 发送的事件值的JSON串
    String json;


    public EventMessage() {
    }

    public EventMessage(final String processName, final String channel, final String event, final String type, final boolean multiProcess) {
        this(processName, channel, event, type, null);
    }

    public EventMessage(final String processName,
                        final String channel,
                        final String event,
                        final String type,
                        final String json
    ) {
        this.processName = processName;
        this.channel = channel;
        this.event = event;
        this.type = type;
        this.json = json;
    }

    protected EventMessage(Parcel in) {
        processName = in.readString();
        channel = in.readString();
        event = in.readString();
        type = in.readString();
        json = in.readString();
    }

    /**
     * 获取唯一值确定一个事件
     *
     * @return key
     */
    String getKey() {
        return channel + event + type;
    }

    @Override
    public String toString() {
        return "{" +
                "processName='" + processName + '\'' +
                ", channel='" + channel + '\'' +
                ", event='" + event + '\'' +
                ", type='" + type + '\'' +
                ", json='" + json + '\'' +
                '}';
    }

    public static final Creator<EventMessage> CREATOR = new Creator<EventMessage>() {
        @Override
        public EventMessage createFromParcel(Parcel in) {
            return new EventMessage(in);
        }

        @Override
        public EventMessage[] newArray(int size) {
            return new EventMessage[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(final Parcel dest, final int flags) {
        dest.writeString(processName);
        dest.writeString(channel);
        dest.writeString(event);
        dest.writeString(type);
        dest.writeString(json);
    }

    public void readFromParcel(Parcel reply) {
        processName = reply.readString();
        channel = reply.readString();
        event = reply.readString();
        type = reply.readString();
        json = reply.readString();
    }
}
