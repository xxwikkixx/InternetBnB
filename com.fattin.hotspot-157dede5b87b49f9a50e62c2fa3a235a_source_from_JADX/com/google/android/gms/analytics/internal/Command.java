package com.google.android.gms.analytics.internal;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class Command implements Parcelable {
    public static final String APPEND_CACHE_BUSTER = "appendCacheBuster";
    public static final String APPEND_QUEUE_TIME = "appendQueueTime";
    public static final String APPEND_VERSION = "appendVersion";
    public static final Creator<Command> CREATOR;
    private String id;
    private String urlParam;
    private String value;

    /* renamed from: com.google.android.gms.analytics.internal.Command.1 */
    static class C00441 implements Creator<Command> {
        C00441() {
        }

        public Command createFromParcel(Parcel in) {
            return new Command(in);
        }

        public Command[] newArray(int size) {
            return new Command[size];
        }
    }

    public Command(String id, String urlParam, String value) {
        this.id = id;
        this.urlParam = urlParam;
        this.value = value;
    }

    public String getId() {
        return this.id;
    }

    public String getUrlParam() {
        return this.urlParam;
    }

    public String getValue() {
        return this.value;
    }

    static {
        CREATOR = new C00441();
    }

    Command(Parcel in) {
        readFromParcel(in);
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeString(this.id);
        out.writeString(this.urlParam);
        out.writeString(this.value);
    }

    private void readFromParcel(Parcel in) {
        this.id = in.readString();
        this.urlParam = in.readString();
        this.value = in.readString();
    }
}
