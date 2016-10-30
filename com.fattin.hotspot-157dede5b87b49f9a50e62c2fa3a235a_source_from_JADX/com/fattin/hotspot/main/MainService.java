package com.fattin.hotspot.main;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.ResultReceiver;
import com.fattin.hotspot.C0000R;
import com.fattin.hotspot.apilib.APIBroadcastReceiver;
import com.fattin.hotspot.app.DetachableResultReceiver;
import com.fattin.hotspot.helpers.Log;
import com.fattin.hotspot.sm.AsyncChannel;
import com.fattin.hotspot.sm.Protocol;
import java.lang.ref.WeakReference;

public class MainService extends Service {
    private static final int BASE = 397312;
    private static final int CMD_EXIT = 397315;
    private static final int CMD_START_BACKGROUND_SERVICES = 397313;
    private static final int CMD_STOP_BACKGROUND_SERVICES = 397314;
    private static final int CMD_STORE_ACTIVITY_RESULT_RECEIVER = 397312;
    private static final int EVENT_BACKGROUND_SERVICES_STARTING = 397325;
    private static final int EVENT_BACKGROUND_SERVICES_STOPPING = 397326;
    private static final int EVENT_SERVICES_STARTED = 397323;
    private static final int EVENT_SERVICES_STARTING_FAILURE = 397327;
    private static final int EVENT_SERVICES_STOPPED = 397324;
    public static final String EXTRA_RESULT_RECEIVER = "com.fattin.hotspot.app.EXTRA_RESULT_RECEIVER";
    public static final String EXTRA_SERVICE_STATUS = "com.fattin.hotspot.app.EXTRA_SERVICE_STATUS";
    private static final int GET_BACKGROUND_SERVICES_STARTING_OR_STOPPING = 397333;
    private static final String LOG_TAG = "MainService";
    private int ONGOING_NOTIFICATION;
    private ResultReceiver mActivityReceiver;
    private MainStateMachine mMainStateMachine;
    private AsyncChannel mMainStateMachineChannel;
    private MainStateMachineHandler mMainStateMachineHandler;
    private Messenger mMessenger;
    private boolean mShouldExit;
    private APIBroadcastReceiver mainApiBroadcastReceiver;
    HandlerThread mainStateMachineHandlerThread;
    HandlerThread mainStateMachineThread;

    /* renamed from: com.fattin.hotspot.main.MainService.2 */
    class C00312 extends AsyncTask<Void, Void, Boolean> {
        C00312() {
        }

        protected void onPreExecute() {
            Log.m3v(MainService.LOG_TAG, "Destroying MainService.....");
            super.onPreExecute();
        }

        protected Boolean doInBackground(Void... params) {
            return Boolean.valueOf(MainService.this.stopBackgroundServices());
        }

        protected void onPostExecute(Boolean result) {
            Log.m3v(MainService.LOG_TAG, "MainService Destroyed");
            super.onPostExecute(result);
        }
    }

    static class IncomingHandler extends Handler {
        private final WeakReference<MainService> mService;

        public IncomingHandler(MainService service) {
            this.mService = new WeakReference(service);
        }

        public void handleMessage(Message msg) {
            MainService service = (MainService) this.mService.get();
            if (service != null) {
                switch (msg.what) {
                    case MainService.CMD_STORE_ACTIVITY_RESULT_RECEIVER /*397312*/:
                        Log.m0d(MainService.LOG_TAG, "IncomingHandler.handleMessage CMD_STORE_ACTIVITY_RESULT_RECEIVER");
                        service.mActivityReceiver = (ResultReceiver) msg.getData().getParcelable(MainService.EXTRA_RESULT_RECEIVER);
                    case MainService.CMD_START_BACKGROUND_SERVICES /*397313*/:
                        Log.m0d(MainService.LOG_TAG, "IncomingHandler.handleMessage CMD_START_BACKGROUND_SERVICES");
                        service.startBackgroundServices();
                    case MainService.CMD_STOP_BACKGROUND_SERVICES /*397314*/:
                        Log.m0d(MainService.LOG_TAG, "IncomingHandler.handleMessage CMD_STOP_BACKGROUND_SERVICES");
                        service.stopBackgroundServices();
                    case MainService.CMD_EXIT /*397315*/:
                        Log.m0d(MainService.LOG_TAG, "IncomingHandler.handleMessage CMD_EXIT");
                        sendMessage(obtainMessage(MainService.CMD_STOP_BACKGROUND_SERVICES));
                        service.mShouldExit = true;
                    case MainService.GET_BACKGROUND_SERVICES_STARTING_OR_STOPPING /*397333*/:
                        Log.m0d(MainService.LOG_TAG, "IncomingHandler.handleMessage GET_BACKGROUND_SERVICES_STARTING_OR_STOPPING");
                        if (service.mMainStateMachine.isStarting()) {
                            service.onPreStartingServices();
                        } else if (service.mMainStateMachine.isStopping()) {
                            service.onPreStoppingServices();
                        }
                    default:
                        super.handleMessage(msg);
                }
            }
        }
    }

    private static class MainStateMachineHandler extends Handler {
        private AsyncChannel mMSsmChannel;
        private final WeakReference<MainService> mService;

        MainStateMachineHandler(Looper looper, MainService service) {
            super(looper);
            this.mService = new WeakReference(service);
            this.mMSsmChannel = new AsyncChannel();
            this.mMSsmChannel.connect((Context) service, (Handler) this, service.mMainStateMachine.getHandler());
        }

        public void handleMessage(Message msg) {
            Log.m0d(MainService.LOG_TAG, "MainStateMachineHandler.handleMessage");
            Context service = (MainService) this.mService.get();
            switch (msg.what) {
                case Protocol.BASE_SYSTEM_ASYNC_CHANNEL /*69632*/:
                    Log.m0d(MainService.LOG_TAG, "MainStateMachineHandler.handleMessage AsyncChannel.CMD_CHANNEL_HALF_CONNECTED");
                    if (msg.arg1 == 0) {
                        Log.m0d(MainService.LOG_TAG, "MainStateMachineHandler.handleMessage AsyncChannel.STATUS_SUCCESSFUL");
                        service.mMainStateMachineChannel = this.mMSsmChannel;
                        service.mMainStateMachineChannel.sendMessage((int) AsyncChannel.CMD_CHANNEL_FULL_CONNECTION);
                        return;
                    }
                    Log.m0d(MainService.LOG_TAG, "MainStateMachineHandler.handleMessage NOT AsyncChannel.STATUS_SUCCESSFUL");
                    Log.m0d(MainService.LOG_TAG, "MainStateMachine connection failure, error=" + msg.arg1);
                    service.mMainStateMachineChannel = null;
                case AsyncChannel.CMD_CHANNEL_DISCONNECTED /*69636*/:
                    Log.m0d(MainService.LOG_TAG, "MainStateMachineHandler.handleMessage AsyncChannel.CMD_CHANNEL_DISCONNECTED");
                    Log.m0d(MainService.LOG_TAG, "MainStateMachine channel lost, msg.arg1 =" + msg.arg1);
                    service.mMainStateMachineChannel = null;
                    this.mMSsmChannel.connect(service, (Handler) this, service.mMainStateMachine.getHandler());
                case MainService.EVENT_SERVICES_STARTED /*397323*/:
                    Log.m0d(MainService.LOG_TAG, "MainStateMachineHandler.handleMessage EVENT_SERVICES_STARTED");
                    service.onPostServicesStarted();
                case MainService.EVENT_SERVICES_STOPPED /*397324*/:
                    Log.m0d(MainService.LOG_TAG, "MainStateMachineHandler.handleMessage EVENT_SERVICES_STOPPED");
                    service.onPostServicesStopped();
                case MainService.EVENT_BACKGROUND_SERVICES_STARTING /*397325*/:
                    Log.m0d(MainService.LOG_TAG, "MainStateMachineHandler.handleMessage EVENT_BACKGROUND_SERVICES_STARTING");
                    service.onPreStartingServices();
                case MainService.EVENT_BACKGROUND_SERVICES_STOPPING /*397326*/:
                    Log.m0d(MainService.LOG_TAG, "MainStateMachineHandler.handleMessage EVENT_BACKGROUND_SERVICES_STOPPING");
                    service.onPreStoppingServices();
                case MainService.EVENT_SERVICES_STARTING_FAILURE /*397327*/:
                    Log.m0d(MainService.LOG_TAG, "MainStateMachineHandler.handleMessage EVENT_SERVICES_STARTING_FAILURE");
                    service.onPostServicesStartingFailure((String) msg.obj);
                    MainService.sendCommandStopBackgroundServices(service.mMessenger);
                default:
                    Log.m0d(MainService.LOG_TAG, "MainStateMachineHandler.handleMessage default");
                    Log.m0d(MainService.LOG_TAG, "MainStateMachineHandler.handleMessage ignoring msg=" + msg);
                    Log.m0d(MainService.LOG_TAG, "MainStateMachineHandler.handleMessage ignoring msg.what=" + msg.what);
            }
        }
    }

    class 1 extends APIBroadcastReceiver {
        1() {
        }

        public void onAPIUserUnauthorized(Context context, Intent intent) {
            Log.m3v(MainService.LOG_TAG, "onAPIUserUnauthorized");
            MainService.this.stopBackgroundServices();
            DetachableResultReceiver.sendUnauthorized(MainService.this.mActivityReceiver, null);
        }
    }

    public MainService() {
        this.ONGOING_NOTIFICATION = C0000R.string.intentservice_started;
        this.mActivityReceiver = null;
        this.mMessenger = null;
        this.mShouldExit = false;
        this.mainApiBroadcastReceiver = new 1();
    }

    private void beginForeground() {
        Notification notification = new Notification(C0000R.drawable.ic_stat_example, getText(C0000R.string.notification_ticker_text), System.currentTimeMillis());
        Intent notificationIntent = new Intent(this, MainActivity.class);
        notificationIntent.setFlags(67108864);
        notification.setLatestEventInfo(this, getText(C0000R.string.notification_title), getText(C0000R.string.notification_message), PendingIntent.getActivity(this, 0, notificationIntent, 0));
        startForeground(this.ONGOING_NOTIFICATION, notification);
    }

    private void cancelForeground() {
        stopForeground(true);
    }

    public void onCreate() {
        this.mMessenger = new Messenger(new IncomingHandler(this));
        this.mainStateMachineThread = new HandlerThread("MainStateMachineThread");
        this.mainStateMachineHandlerThread = new HandlerThread("MainStateMachineHandlerThread");
        initHandlerThreads();
        APIBroadcastReceiver.registerAPIBroadcastReceiver(this, this.mainApiBroadcastReceiver);
        super.onCreate();
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            Log.m3v(LOG_TAG, "intent=" + intent.toString());
        } else {
            Log.m3v(LOG_TAG, "intent is null");
        }
        return 1;
    }

    public IBinder onBind(Intent intent) {
        return this.mMessenger.getBinder();
    }

    public void onDestroy() {
        new C00312().execute(new Void[0]);
        APIBroadcastReceiver.unregisterAPIBroadcastReceiver(this, this.mainApiBroadcastReceiver);
        super.onDestroy();
    }

    private synchronized void onPreStartingServices() {
        Log.m3v(LOG_TAG, "onPreStartingServices");
        beginForeground();
        DetachableResultReceiver.sendStartingServices(this.mActivityReceiver, null);
    }

    private synchronized void onPreStoppingServices() {
        Log.m3v(LOG_TAG, "onPreStoppingServices");
        DetachableResultReceiver.sendStoppingServices(this.mActivityReceiver, null);
    }

    private synchronized void onPostServicesStarted() {
        Log.m3v(LOG_TAG, "onPostServicesStarted");
        DetachableResultReceiver.sendServiceStarted(this.mActivityReceiver, null);
    }

    private synchronized void onPostServicesStopped() {
        Log.m3v(LOG_TAG, "onPostServicesStopped");
        DetachableResultReceiver.sendServiceStopped(this.mActivityReceiver, null);
        cancelForeground();
        if (this.mShouldExit) {
            stopSelf();
        }
    }

    public void onPostServicesStartingFailure(String error_msg) {
        Log.m3v(LOG_TAG, "onPostServicesStartingFailure");
        DetachableResultReceiver.sendServiceStartingFailure(this.mActivityReceiver, error_msg, null);
    }

    private synchronized boolean startBackgroundServices() {
        Log.m3v(LOG_TAG, "startBackgroundServices");
        initHandlerThreads();
        return this.mMainStateMachine.sendCommandStartServices(this.mMainStateMachineChannel);
    }

    private synchronized boolean stopBackgroundServices() {
        return this.mMainStateMachine.sendCommandStopServices(this.mMainStateMachineChannel);
    }

    public static boolean sendCommandStartBackgroundServices(Messenger mMessenger) {
        Log.m0d(LOG_TAG, "sendCommandStartBackgroundServices");
        try {
            mMessenger.send(Message.obtain(null, CMD_START_BACKGROUND_SERVICES, 0, 0));
            return true;
        } catch (Exception e) {
            Log.m1e(LOG_TAG, e.getMessage(), e);
            return false;
        }
    }

    public static boolean sendCommandStopBackgroundServices(Messenger mMessenger) {
        Log.m0d(LOG_TAG, "sendCommandStopBackgroundServices");
        try {
            mMessenger.send(Message.obtain(null, CMD_STOP_BACKGROUND_SERVICES, 0, 0));
            return true;
        } catch (Exception e) {
            Log.m1e(LOG_TAG, e.getMessage(), e);
            return false;
        }
    }

    public static boolean sendCommandExit(Messenger mMessenger) {
        Log.m0d(LOG_TAG, "sendCommandExit");
        try {
            mMessenger.send(Message.obtain(null, CMD_EXIT, 0, 0));
            return true;
        } catch (Exception e) {
            Log.m1e(LOG_TAG, e.getMessage(), e);
            return false;
        }
    }

    public static boolean sendCommandStoreResultReceiver(Messenger mMessenger, DetachableResultReceiver mActivityResultReceiver) {
        Log.m0d(LOG_TAG, "sendCommandStoreResultReceiver");
        Bundle b = new Bundle();
        b.putParcelable(EXTRA_RESULT_RECEIVER, mActivityResultReceiver);
        Message msg = Message.obtain(null, CMD_STORE_ACTIVITY_RESULT_RECEIVER, 0, 0);
        msg.setData(b);
        try {
            mMessenger.send(msg);
            return true;
        } catch (Exception e) {
            Log.m1e(LOG_TAG, e.getMessage(), e);
            return false;
        }
    }

    public static boolean getServicesStartingOrStopping(Messenger mMessenger) {
        Log.m0d(LOG_TAG, "getServicesStartingOrStopping");
        if (!(mMessenger instanceof Messenger)) {
            return false;
        }
        try {
            mMessenger.send(Message.obtain(null, GET_BACKGROUND_SERVICES_STARTING_OR_STOPPING, 0, 0));
            return true;
        } catch (Exception e) {
            Log.m1e(LOG_TAG, e.getMessage(), e);
            return false;
        }
    }

    public static boolean sendEventServicesStarting(AsyncChannel channel) {
        Log.m0d(LOG_TAG, "sendEventServicesStarting");
        if (!(channel instanceof AsyncChannel)) {
            return false;
        }
        channel.sendMessage((int) EVENT_BACKGROUND_SERVICES_STARTING);
        Log.m0d(LOG_TAG, "sendEventServicesStarting true");
        return true;
    }

    public static boolean sendEventServicesStopping(AsyncChannel channel) {
        Log.m0d(LOG_TAG, "sendEventServicesStopping");
        if (!(channel instanceof AsyncChannel)) {
            return false;
        }
        channel.sendMessage((int) EVENT_BACKGROUND_SERVICES_STOPPING);
        Log.m0d(LOG_TAG, "sendEventServicesStopping true");
        return true;
    }

    public static boolean sendEventServicesStarted(AsyncChannel channel) {
        Log.m0d(LOG_TAG, "sendEventServicesStarted");
        if (!(channel instanceof AsyncChannel)) {
            return false;
        }
        channel.sendMessage((int) EVENT_SERVICES_STARTED);
        Log.m0d(LOG_TAG, "sendEventServicesStarted true");
        return true;
    }

    public static boolean sendEventServicesStopped(AsyncChannel channel) {
        Log.m0d(LOG_TAG, "sendEventServicesStopped");
        if (!(channel instanceof AsyncChannel)) {
            return false;
        }
        channel.sendMessage((int) EVENT_SERVICES_STOPPED);
        Log.m0d(LOG_TAG, "sendEventServicesStopped true");
        return true;
    }

    public static boolean sendEventServicesStartingFailure(AsyncChannel channel, String error_msg) {
        Log.m0d(LOG_TAG, "sendEventServicesStartingFailure");
        if (!(channel instanceof AsyncChannel)) {
            return false;
        }
        channel.sendMessage((int) EVENT_SERVICES_STARTING_FAILURE, (Object) error_msg);
        Log.m0d(LOG_TAG, "sendEventServicesStartingFailure true");
        return true;
    }

    private void initHandlerThreads() {
        Log.m3v(LOG_TAG, "initHandlerThreads");
        if (!this.mainStateMachineThread.isAlive()) {
            Log.m3v(LOG_TAG, "initHandlerThreads -> mainStateMachineThread is dead");
            this.mainStateMachineThread.start();
            this.mMainStateMachine = new MainStateMachine(this.mainStateMachineThread.getLooper(), this);
        }
        if (!this.mainStateMachineHandlerThread.isAlive()) {
            Log.m3v(LOG_TAG, "initHandlerThreads -> mainStateMachineHandlerThread is dead");
            Log.m3v(LOG_TAG, "initHandlerThreads");
            this.mainStateMachineHandlerThread.start();
            this.mMainStateMachineHandler = new MainStateMachineHandler(this.mainStateMachineHandlerThread.getLooper(), this);
        }
    }
}
