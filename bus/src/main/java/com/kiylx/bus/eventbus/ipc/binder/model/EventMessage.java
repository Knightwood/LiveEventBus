package com.kiylx.bus.eventbus.ipc.binder.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 进程间缓存事件封装类，事件定义
 */
public class EventMessage implements Parcelable {

   public String dataFrom;
   public String dataTo;
   public String channelName;
   public String connectService;
   public String dataType;
   public String json;


    public EventMessage() {
    }

    public EventMessage(final String channelName,
                        final String type) {
        this(channelName,  type, null);
    }

    public EventMessage(final String channelName,
                        final String type,
                        final String json
    ) {
        this.channelName = channelName;
        this.type = type;
        this.json = json;
    }

    protected EventMessage(Parcel in) {
        channelName = in.readString();
        type = in.readString();
        json = in.readString();
    }

    @Override
    public String toString() {
        return "{" +
                ", channel='" + channelName + '\'' +
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
        dest.writeString(channelName);
        dest.writeString(type);
        dest.writeString(json);
    }

    public void readFromParcel(Parcel reply) {
        channelName = reply.readString();
        type = reply.readString();
        json = reply.readString();
    }
}

