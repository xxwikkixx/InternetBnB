package com.fattin.hotspot.sm;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import com.fattin.hotspot.helpers.Log;

public abstract class AsyncService extends Service {
    public static final int CMD_ASYNC_SERVICE_DESTROY = 16777216;
    public static final int CMD_ASYNC_SERVICE_ON_START_INTENT = 16777215;
    private static final String TAG = "AsyncService";
    AsyncServiceInfo mAsyncServiceInfo;
    Handler mHandler;
    protected Messenger mMessenger;

    public static final class AsyncServiceInfo {
        public Handler mHandler;
        public int mRestartFlags;
    }

    public abstract AsyncServiceInfo createHandler();

    public Handler getHandler() {
        return this.mHandler;
    }

    public void onCreate() {
        super.onCreate();
        this.mAsyncServiceInfo = createHandler();
        this.mHandler = this.mAsyncServiceInfo.mHandler;
        this.mMessenger = new Messenger(this.mHandler);
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.m0d(TAG, "onStartCommand");
        Message msg = this.mHandler.obtainMessage();
        msg.what = CMD_ASYNC_SERVICE_ON_START_INTENT;
        msg.arg1 = flags;
        msg.arg2 = startId;
        msg.obj = intent;
        this.mHandler.sendMessage(msg);
        return this.mAsyncServiceInfo.mRestartFlags;
    }

    public void onDestroy() {
        Log.m0d(TAG, "onDestroy");
        Message msg = this.mHandler.obtainMessage();
        msg.what = CMD_ASYNC_SERVICE_DESTROY;
        this.mHandler.sendMessage(msg);
    }

    public IBinder onBind(Intent intent) {
        return this.mMessenger.getBinder();
    }
}
