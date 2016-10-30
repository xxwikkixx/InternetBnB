package com.fattin.hotspot.sm;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import com.fattin.hotspot.helpers.Log;
import java.util.Stack;

public class AsyncChannel {
    private static final int BASE = 69632;
    public static final int CMD_CHANNEL_DISCONNECT = 69635;
    public static final int CMD_CHANNEL_DISCONNECTED = 69636;
    public static final int CMD_CHANNEL_FULLY_CONNECTED = 69634;
    public static final int CMD_CHANNEL_FULL_CONNECTION = 69633;
    public static final int CMD_CHANNEL_HALF_CONNECTED = 69632;
    private static final boolean DBG = false;
    public static final int STATUS_BINDING_UNSUCCESSFUL = 1;
    public static final int STATUS_FULL_CONNECTION_REFUSED_ALREADY_CONNECTED = 3;
    public static final int STATUS_SEND_UNSUCCESSFUL = 2;
    public static final int STATUS_SUCCESSFUL = 0;
    private static final String TAG = "AsyncChannel";
    private AsyncChannelConnection mConnection;
    private Messenger mDstMessenger;
    private Context mSrcContext;
    private Handler mSrcHandler;
    private Messenger mSrcMessenger;

    final class 1ConnectAsync implements Runnable {
        String mDstClassName;
        String mDstPackageName;
        Context mSrcCtx;
        Handler mSrcHdlr;

        1ConnectAsync(Context srcContext, Handler srcHandler, String dstPackageName, String dstClassName) {
            this.mSrcCtx = srcContext;
            this.mSrcHdlr = srcHandler;
            this.mDstPackageName = dstPackageName;
            this.mDstClassName = dstClassName;
        }

        public void run() {
            AsyncChannel.this.replyHalfConnected(AsyncChannel.this.connectSrcHandlerToPackageSync(this.mSrcCtx, this.mSrcHdlr, this.mDstPackageName, this.mDstClassName));
        }
    }

    class AsyncChannelConnection implements ServiceConnection {
        AsyncChannelConnection() {
        }

        public void onServiceConnected(ComponentName className, IBinder service) {
            AsyncChannel.this.mDstMessenger = new Messenger(service);
            AsyncChannel.this.replyHalfConnected(AsyncChannel.STATUS_SUCCESSFUL);
        }

        public void onServiceDisconnected(ComponentName className) {
            AsyncChannel.this.replyDisconnected(AsyncChannel.STATUS_SUCCESSFUL);
        }
    }

    private static class SyncMessenger {
        private static int sCount;
        private static Stack<SyncMessenger> sStack;
        private SyncHandler mHandler;
        private HandlerThread mHandlerThread;
        private Messenger mMessenger;

        private static class SyncHandler extends Handler {
            private Object mLockObject;
            private Message mResultMsg;

            private SyncHandler(Looper looper) {
                super(looper);
                this.mLockObject = new Object();
            }

            public void handleMessage(Message msg) {
                this.mResultMsg = Message.obtain();
                this.mResultMsg.copyFrom(msg);
                synchronized (this.mLockObject) {
                    this.mLockObject.notify();
                }
            }
        }

        static {
            sStack = new Stack();
            sCount = AsyncChannel.STATUS_SUCCESSFUL;
        }

        private SyncMessenger() {
        }

        private static SyncMessenger obtain() {
            SyncMessenger sm;
            synchronized (sStack) {
                if (sStack.isEmpty()) {
                    sm = new SyncMessenger();
                    StringBuilder stringBuilder = new StringBuilder("SyncHandler-");
                    int i = sCount;
                    sCount = i + AsyncChannel.STATUS_BINDING_UNSUCCESSFUL;
                    sm.mHandlerThread = new HandlerThread(stringBuilder.append(i).toString());
                    sm.mHandlerThread.start();
                    sm.mHandler = new SyncHandler(null);
                    sm.mMessenger = new Messenger(sm.mHandler);
                } else {
                    sm = (SyncMessenger) sStack.pop();
                }
            }
            return sm;
        }

        private void recycle() {
            synchronized (sStack) {
                sStack.push(this);
            }
        }

        private static Message sendMessageSynchronously(Messenger dstMessenger, Message msg) {
            SyncMessenger sm = obtain();
            if (dstMessenger == null || msg == null) {
                sm.mHandler.mResultMsg = null;
            } else {
                try {
                    msg.replyTo = sm.mMessenger;
                    synchronized (sm.mHandler.mLockObject) {
                        dstMessenger.send(msg);
                        sm.mHandler.mLockObject.wait();
                    }
                } catch (InterruptedException e) {
                    sm.mHandler.mResultMsg = null;
                } catch (RemoteException e2) {
                    sm.mHandler.mResultMsg = null;
                }
            }
            Message resultMsg = sm.mHandler.mResultMsg;
            sm.recycle();
            return resultMsg;
        }
    }

    public int connectSrcHandlerToPackageSync(Context srcContext, Handler srcHandler, String dstPackageName, String dstClassName) {
        this.mConnection = new AsyncChannelConnection();
        this.mSrcContext = srcContext;
        this.mSrcHandler = srcHandler;
        this.mSrcMessenger = new Messenger(srcHandler);
        this.mDstMessenger = null;
        Intent intent = new Intent("android.intent.action.MAIN");
        intent.setClassName(dstPackageName, dstClassName);
        if (srcContext.bindService(intent, this.mConnection, STATUS_BINDING_UNSUCCESSFUL)) {
            return STATUS_SUCCESSFUL;
        }
        return STATUS_BINDING_UNSUCCESSFUL;
    }

    public int connectSync(Context srcContext, Handler srcHandler, Messenger dstMessenger) {
        connected(srcContext, srcHandler, dstMessenger);
        return STATUS_SUCCESSFUL;
    }

    public int connectSync(Context srcContext, Handler srcHandler, Handler dstHandler) {
        return connectSync(srcContext, srcHandler, new Messenger(dstHandler));
    }

    public int fullyConnectSync(Context srcContext, Handler srcHandler, Handler dstHandler) {
        int status = connectSync(srcContext, srcHandler, dstHandler);
        if (status == 0) {
            return sendMessageSynchronously((int) CMD_CHANNEL_FULL_CONNECTION).arg1;
        }
        return status;
    }

    public void connect(Context srcContext, Handler srcHandler, String dstPackageName, String dstClassName) {
        new Thread(new 1ConnectAsync(srcContext, srcHandler, dstPackageName, dstClassName)).start();
    }

    public void connect(Context srcContext, Handler srcHandler, Class<?> klass) {
        connect(srcContext, srcHandler, klass.getPackage().getName(), klass.getName());
    }

    public void connect(Context srcContext, Handler srcHandler, Messenger dstMessenger) {
        connected(srcContext, srcHandler, dstMessenger);
        replyHalfConnected(STATUS_SUCCESSFUL);
    }

    public void connected(Context srcContext, Handler srcHandler, Messenger dstMessenger) {
        this.mSrcContext = srcContext;
        this.mSrcHandler = srcHandler;
        this.mSrcMessenger = new Messenger(this.mSrcHandler);
        this.mDstMessenger = dstMessenger;
    }

    public void connect(Context srcContext, Handler srcHandler, Handler dstHandler) {
        connect(srcContext, srcHandler, new Messenger(dstHandler));
    }

    public void connect(AsyncService srcAsyncService, Messenger dstMessenger) {
        connect((Context) srcAsyncService, srcAsyncService.getHandler(), dstMessenger);
    }

    public void disconnected() {
        this.mSrcContext = null;
        this.mSrcHandler = null;
        this.mSrcMessenger = null;
        this.mDstMessenger = null;
        this.mConnection = null;
    }

    public void disconnect() {
        if (!(this.mConnection == null || this.mSrcContext == null)) {
            this.mSrcContext.unbindService(this.mConnection);
        }
        if (this.mSrcHandler != null) {
            replyDisconnected(STATUS_SUCCESSFUL);
        }
    }

    public void sendMessage(Message msg) {
        msg.replyTo = this.mSrcMessenger;
        try {
            this.mDstMessenger.send(msg);
        } catch (RemoteException e) {
            replyDisconnected(STATUS_SEND_UNSUCCESSFUL);
        }
    }

    public void sendMessage(int what) {
        Message msg = Message.obtain();
        msg.what = what;
        sendMessage(msg);
    }

    public void sendMessage(int what, int arg1) {
        Message msg = Message.obtain();
        msg.what = what;
        msg.arg1 = arg1;
        sendMessage(msg);
    }

    public void sendMessage(int what, int arg1, int arg2) {
        Message msg = Message.obtain();
        msg.what = what;
        msg.arg1 = arg1;
        msg.arg2 = arg2;
        sendMessage(msg);
    }

    public void sendMessage(int what, int arg1, int arg2, Object obj) {
        Message msg = Message.obtain();
        msg.what = what;
        msg.arg1 = arg1;
        msg.arg2 = arg2;
        msg.obj = obj;
        sendMessage(msg);
    }

    public void sendMessage(int what, Object obj) {
        Message msg = Message.obtain();
        msg.what = what;
        msg.obj = obj;
        sendMessage(msg);
    }

    public void replyToMessage(Message srcMsg, Message dstMsg) {
        try {
            dstMsg.replyTo = this.mSrcMessenger;
            srcMsg.replyTo.send(dstMsg);
        } catch (RemoteException e) {
            log("TODO: handle replyToMessage RemoteException" + e);
            e.printStackTrace();
        }
    }

    public void replyToMessage(Message srcMsg, int what) {
        Message msg = Message.obtain();
        msg.what = what;
        replyToMessage(srcMsg, msg);
    }

    public void replyToMessage(Message srcMsg, int what, int arg1) {
        Message msg = Message.obtain();
        msg.what = what;
        msg.arg1 = arg1;
        replyToMessage(srcMsg, msg);
    }

    public void replyToMessage(Message srcMsg, int what, int arg1, int arg2) {
        Message msg = Message.obtain();
        msg.what = what;
        msg.arg1 = arg1;
        msg.arg2 = arg2;
        replyToMessage(srcMsg, msg);
    }

    public void replyToMessage(Message srcMsg, int what, int arg1, int arg2, Object obj) {
        Message msg = Message.obtain();
        msg.what = what;
        msg.arg1 = arg1;
        msg.arg2 = arg2;
        msg.obj = obj;
        replyToMessage(srcMsg, msg);
    }

    public void replyToMessage(Message srcMsg, int what, Object obj) {
        Message msg = Message.obtain();
        msg.what = what;
        msg.obj = obj;
        replyToMessage(srcMsg, msg);
    }

    public Message sendMessageSynchronously(Message msg) {
        return SyncMessenger.sendMessageSynchronously(this.mDstMessenger, msg);
    }

    public Message sendMessageSynchronously(int what) {
        Message msg = Message.obtain();
        msg.what = what;
        return sendMessageSynchronously(msg);
    }

    public Message sendMessageSynchronously(int what, int arg1) {
        Message msg = Message.obtain();
        msg.what = what;
        msg.arg1 = arg1;
        return sendMessageSynchronously(msg);
    }

    public Message sendMessageSynchronously(int what, int arg1, int arg2) {
        Message msg = Message.obtain();
        msg.what = what;
        msg.arg1 = arg1;
        msg.arg2 = arg2;
        return sendMessageSynchronously(msg);
    }

    public Message sendMessageSynchronously(int what, int arg1, int arg2, Object obj) {
        Message msg = Message.obtain();
        msg.what = what;
        msg.arg1 = arg1;
        msg.arg2 = arg2;
        msg.obj = obj;
        return sendMessageSynchronously(msg);
    }

    public Message sendMessageSynchronously(int what, Object obj) {
        Message msg = Message.obtain();
        msg.what = what;
        msg.obj = obj;
        return sendMessageSynchronously(msg);
    }

    private void replyHalfConnected(int status) {
        Message msg = this.mSrcHandler.obtainMessage(CMD_CHANNEL_HALF_CONNECTED);
        msg.arg1 = status;
        msg.obj = this;
        msg.replyTo = this.mDstMessenger;
        this.mSrcHandler.sendMessage(msg);
    }

    private void replyDisconnected(int status) {
        Message msg = this.mSrcHandler.obtainMessage(CMD_CHANNEL_DISCONNECTED);
        msg.arg1 = status;
        msg.obj = this;
        msg.replyTo = this.mDstMessenger;
        this.mSrcHandler.sendMessage(msg);
    }

    private static void log(String s) {
        Log.m0d(TAG, s);
    }
}
