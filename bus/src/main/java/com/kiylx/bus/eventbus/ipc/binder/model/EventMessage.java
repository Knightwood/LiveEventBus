package com.kiylx.bus.eventbus.ipc.binder.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 进程间缓存事件封装类，事件定义
 */
public class EventMessage implements Parcelable {
    // 事件属于哪个channel
    public String channel;
    // 发送的事件类型
    public String type;
    // 发送的事件值的JSON串
    public String json;


    public EventMessage() {
    }

    public EventMessage(final String channel,
                        final String type) {
        this( channel,  type, null);
    }

    public EventMessage(final String channel,
                        final String type,
                        final String json
    ) {
        this.channel = channel;
        this.type = type;
        this.json = json;
    }

    protected EventMessage(Parcel in) {
        channel = in.readString();
        type = in.readString();
        json = in.readString();
    }

    @Override
    public String toString() {
        return "{" +
                ", channel='" + channel + '\'' +
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
        dest.writeString(channel);
        dest.writeString(type);
        dest.writeString(json);
    }

    public void readFromParcel(Parcel reply) {
        channel = reply.readString();
        type = reply.readString();
        json = reply.readString();
    }
}
