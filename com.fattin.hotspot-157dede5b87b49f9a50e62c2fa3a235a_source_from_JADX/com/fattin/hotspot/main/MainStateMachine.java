package com.fattin.hotspot.main;

import android.content.Context;
import android.os.Looper;
import android.os.Message;
import com.fattin.hotspot.app.GlobalStates;
import com.fattin.hotspot.app.MainApplication;
import com.fattin.hotspot.connectivity.WifiAPManager;
import com.fattin.hotspot.helpers.Log;
import com.fattin.hotspot.sm.AsyncChannel;
import com.fattin.hotspot.sm.Protocol;
import com.fattin.hotspot.sm.State;
import com.fattin.hotspot.sm.StateMachine;

public class MainStateMachine extends StateMachine {
    private static final int BASE = 401408;
    private static final int CMD_STARTING_SERVICES = 401409;
    private static final int CMD_START_SERVICES = 401410;
    private static final int CMD_STOPPING_SERVICES = 401411;
    private static final int CMD_STOP_SERVICES = 401412;
    private static final int EVENT_CONNECTIVITY_MONITOR_STARTED = 401421;
    private static final int EVENT_CONNECTIVITY_MONITOR_STARTING_FAILURE = 401423;
    private static final int EVENT_CONNECTIVITY_MONITOR_STOPPED = 401422;
    private static final int EVENT_PREREQUISITES_CHECKED = 401424;
    private static final int EVENT_WEBSERVERS_STARTED = 401419;
    private static final int EVENT_WEBSERVERS_STOPPED = 401420;
    private static final int FAILURE = -1;
    private static final String LOG_TAG = "MainStateMachine";
    private static final int SUCCESS = 1;
    ConnectivityMonitorStateMachine mConnectivityMonitorStateMachine;
    private Context mContext;
    private DefaultState mDefaultState;
    PrerequisitesStateMachine mPrerequisitesStateMachine;
    public AsyncChannel mServiceAsyncChannel;
    private StartedState mStartedState;
    private StartingState mStartingState;
    private StoppedState mStoppedState;
    private StoppingState mStoppingState;
    WebServersStateMachine mWebServersStateMachine;

    private class DefaultState extends State {
        private DefaultState() {
        }

        public void enter() {
            Log.m3v(MainStateMachine.LOG_TAG, "DefaultState.enter");
        }

        public boolean processMessage(Message msg) {
            Log.m3v(MainStateMachine.LOG_TAG, "DefaultState.processMessage " + msg.what);
            switch (msg.what) {
                case Protocol.BASE_SYSTEM_ASYNC_CHANNEL /*69632*/:
                    Log.m0d(MainStateMachine.LOG_TAG, "DefaultState.processMessage AsyncChannel.CMD_CHANNEL_HALF_CONNECTED");
                    if (msg.arg1 != 0) {
                        Log.m0d(MainStateMachine.LOG_TAG, "DefaultState.processMessage NOT AsyncChannel.STATUS_SUCCESSFUL");
                        break;
                    }
                    Log.m0d(MainStateMachine.LOG_TAG, "DefaultState.processMessage AsyncChannel.STATUS_SUCCESSFUL");
                    MainStateMachine.this.mServiceAsyncChannel = (AsyncChannel) msg.obj;
                    break;
                case AsyncChannel.CMD_CHANNEL_FULL_CONNECTION /*69633*/:
                    Log.m0d(MainStateMachine.LOG_TAG, "DefaultState.handleMessage AsyncChannel.CMD_CHANNEL_FULL_CONNECTION");
                    new AsyncChannel().connect(MainStateMachine.this.mContext, MainStateMachine.this.getHandler(), msg.replyTo);
                    break;
                case AsyncChannel.CMD_CHANNEL_DISCONNECTED /*69636*/:
                    Log.m0d(MainStateMachine.LOG_TAG, "DefaultState.processMessage AsyncChannel.CMD_CHANNEL_DISCONNECTED");
                    MainStateMachine.this.mServiceAsyncChannel = null;
                    break;
                case MainStateMachine.CMD_START_SERVICES /*401410*/:
                    Log.m0d(MainStateMachine.LOG_TAG, "DefaultState.processMessage CMD_START_SERVICES");
                    MainService.sendEventServicesStarting(MainStateMachine.this.mServiceAsyncChannel);
                    MainStateMachine.this.sendMessage((int) MainStateMachine.CMD_STARTING_SERVICES);
                    MainStateMachine.this.transitionTo(MainStateMachine.this.mStartingState);
                    break;
                case MainStateMachine.CMD_STOP_SERVICES /*401412*/:
                    Log.m0d(MainStateMachine.LOG_TAG, "DefaultState.processMessage CMD_STOP_SERVICES");
                    MainService.sendEventServicesStopping(MainStateMachine.this.mServiceAsyncChannel);
                    MainStateMachine.this.sendMessage((int) MainStateMachine.CMD_STOPPING_SERVICES);
                    MainStateMachine.this.transitionTo(MainStateMachine.this.mStoppingState);
                    break;
            }
            return true;
        }

        public void exit() {
            Log.m3v(MainStateMachine.LOG_TAG, "DefaultState.exit");
        }
    }

    private class StartedState extends State {
        private StartedState() {
        }

        public void enter() {
            Log.m3v(MainStateMachine.LOG_TAG, "StartedState.enter");
            GlobalStates.setServiceState(GlobalStates.State.ENABLED);
        }

        public boolean processMessage(Message msg) {
            Log.m3v(MainStateMachine.LOG_TAG, "StartedState.processMessage " + msg.what);
            switch (msg.what) {
                case MainStateMachine.CMD_STARTING_SERVICES /*401409*/:
                case MainStateMachine.CMD_START_SERVICES /*401410*/:
                    MainService.sendEventServicesStarted(MainStateMachine.this.mServiceAsyncChannel);
                    return true;
                default:
                    return false;
            }
        }

        public void exit() {
            Log.m3v(MainStateMachine.LOG_TAG, "StartedState.exit");
        }
    }

    private class StartingState extends State {
        private StartingState() {
        }

        public void enter() {
            Log.m3v(MainStateMachine.LOG_TAG, "StartingState.enter");
            GlobalStates.setServiceState(GlobalStates.State.ENABLING);
        }

        public boolean processMessage(Message msg) {
            Log.m3v(MainStateMachine.LOG_TAG, "StartingState.processMessage " + msg.what);
            switch (msg.what) {
                case MainStateMachine.CMD_STARTING_SERVICES /*401409*/:
                    Log.m0d(MainStateMachine.LOG_TAG, "StartingState.handleMessage CMD_STARTING_SERVICES");
                    MainStateMachine.this.mPrerequisitesStateMachine.sendCommandCheckPrerequisites();
                    break;
                case MainStateMachine.CMD_START_SERVICES /*401410*/:
                    break;
                case MainStateMachine.EVENT_WEBSERVERS_STARTED /*401419*/:
                    Log.m0d(MainStateMachine.LOG_TAG, "StartingState.handleMessage EVENT_WEBSERVERS_STARTED");
                    MainStateMachine.this.mConnectivityMonitorStateMachine.sendCommandStartConnectivityMonitor();
                    break;
                case MainStateMachine.EVENT_CONNECTIVITY_MONITOR_STARTED /*401421*/:
                    Log.m0d(MainStateMachine.LOG_TAG, "StartingState.handleMessage EVENT_CONNECTIVITY_MONITOR_STARTED");
                    MainStateMachine.this.sendMessage((int) MainStateMachine.CMD_STARTING_SERVICES);
                    MainStateMachine.this.transitionTo(MainStateMachine.this.mStartedState);
                    break;
                case MainStateMachine.EVENT_CONNECTIVITY_MONITOR_STARTING_FAILURE /*401423*/:
                    Log.m0d(MainStateMachine.LOG_TAG, "StartingState.handleMessage EVENT_CONNECTIVITY_MONITOR_STARTING_FAILURE");
                    MainService.sendEventServicesStartingFailure(MainStateMachine.this.mServiceAsyncChannel, "Couldn't connect to server.\nTry again later.");
                    break;
                case MainStateMachine.EVENT_PREREQUISITES_CHECKED /*401424*/:
                    Log.m0d(MainStateMachine.LOG_TAG, "StartingState.handleMessage EVENT_PREREQUISITES_CHECKED");
                    MainStateMachine.this.mWebServersStateMachine.sendCommandStartWebServers();
                    break;
                default:
                    return false;
            }
            return true;
        }

        public void exit() {
            Log.m3v(MainStateMachine.LOG_TAG, "StartingState.exit");
        }
    }

    private class StoppedState extends State {
        private StoppedState() {
        }

        public void enter() {
            Log.m3v(MainStateMachine.LOG_TAG, "StoppedState.enter");
            GlobalStates.setServiceState(GlobalStates.State.DISABLED);
        }

        public boolean processMessage(Message msg) {
            Log.m3v(MainStateMachine.LOG_TAG, "StoppedState.processMessage " + msg.what);
            switch (msg.what) {
                case MainStateMachine.CMD_STOPPING_SERVICES /*401411*/:
                case MainStateMachine.CMD_STOP_SERVICES /*401412*/:
                    MainService.sendEventServicesStopped(MainStateMachine.this.mServiceAsyncChannel);
                    return true;
                default:
                    return false;
            }
        }

        public void exit() {
            Log.m3v(MainStateMachine.LOG_TAG, "StoppedState.exit");
        }
    }

    private class StoppingState extends State {
        private StoppingState() {
        }

        public void enter() {
            Log.m3v(MainStateMachine.LOG_TAG, "StoppingState.enter");
            GlobalStates.setServiceState(GlobalStates.State.DISABLING);
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public boolean processMessage(android.os.Message r4) {
            /*
            r3 = this;
            r0 = "MainStateMachine";
            r1 = new java.lang.StringBuilder;
            r2 = "StoppingState.processMessage ";
            r1.<init>(r2);
            r2 = r4.what;
            r1 = r1.append(r2);
            r1 = r1.toString();
            com.fattin.hotspot.helpers.Log.m3v(r0, r1);
            r0 = r4.what;
            switch(r0) {
                case 401411: goto L_0x001d;
                case 401412: goto L_0x0046;
                case 401420: goto L_0x002b;
                case 401422: goto L_0x0024;
                default: goto L_0x001b;
            };
        L_0x001b:
            r0 = 0;
        L_0x001c:
            return r0;
        L_0x001d:
            r0 = com.fattin.hotspot.main.MainStateMachine.this;
            r0 = r0.mConnectivityMonitorStateMachine;
            r0.sendCommandStopConnectivityMonitor();
        L_0x0024:
            r0 = com.fattin.hotspot.main.MainStateMachine.this;
            r0 = r0.mWebServersStateMachine;
            r0.sendCommandStopWebServers();
        L_0x002b:
            r0 = com.fattin.hotspot.main.MainStateMachine.this;
            r0.stopAndRestoreWifiAp();
            com.fattin.hotspot.netfilter.AccessListDatabase.clear();
            r0 = com.fattin.hotspot.main.MainStateMachine.this;
            r1 = 401411; // 0x62003 float:5.62497E-40 double:1.983234E-318;
            r0.sendMessage(r1);
            r0 = com.fattin.hotspot.main.MainStateMachine.this;
            r1 = com.fattin.hotspot.main.MainStateMachine.this;
            r1 = r1.mStoppedState;
            r0.transitionTo(r1);
        L_0x0046:
            r0 = 1;
            goto L_0x001c;
            */
            throw new UnsupportedOperationException("Method not decompiled: com.fattin.hotspot.main.MainStateMachine.StoppingState.processMessage(android.os.Message):boolean");
        }

        public void exit() {
            Log.m3v(MainStateMachine.LOG_TAG, "StoppingState.exit");
        }
    }

    protected MainStateMachine(Looper looper, Context context) {
        super(LOG_TAG, looper);
        this.mDefaultState = new DefaultState();
        this.mStartingState = new StartingState();
        this.mStartedState = new StartedState();
        this.mStoppingState = new StoppingState();
        this.mStoppedState = new StoppedState();
        this.mContext = context;
        addState(this.mDefaultState);
        addState(this.mStartingState, this.mDefaultState);
        addState(this.mStartedState, this.mStartingState);
        addState(this.mStoppingState, this.mDefaultState);
        addState(this.mStoppedState, this.mStoppingState);
        setInitialState(this.mStoppedState);
        start();
        this.mWebServersStateMachine = new WebServersStateMachine(this.mContext, this, getHandler());
        this.mConnectivityMonitorStateMachine = new ConnectivityMonitorStateMachine(this.mContext, this, getHandler());
        this.mPrerequisitesStateMachine = new PrerequisitesStateMachine(this.mContext, this, getHandler());
        GlobalStates.setServiceState(GlobalStates.State.UNKNOWN);
    }

    private boolean stopAndRestoreWifiAp() {
        WifiAPManager.getInstance(MainApplication.getAppContext()).stopWifiAp();
        return true;
    }

    public boolean sendCommandStartServices(AsyncChannel channel) {
        Log.m3v(LOG_TAG, "sendCommandStartServices");
        if (!(channel instanceof AsyncChannel)) {
            return false;
        }
        channel.sendMessage((int) CMD_START_SERVICES);
        Log.m3v(LOG_TAG, "sendCommandStartServices result=true");
        return true;
    }

    public boolean sendCommandStopServices(AsyncChannel channel) {
        Log.m3v(LOG_TAG, "sendCommandStopServices");
        if (!(channel instanceof AsyncChannel)) {
            return false;
        }
        channel.sendMessage((int) CMD_STOP_SERVICES);
        Log.m3v(LOG_TAG, "sendCommandStopServices result=true");
        return true;
    }

    public boolean sendEventWebServersStarted() {
        sendMessage((int) EVENT_WEBSERVERS_STARTED);
        return true;
    }

    public boolean sendEventWebServersStopped() {
        sendMessage((int) EVENT_WEBSERVERS_STOPPED);
        return true;
    }

    public boolean sendEventConnectivityMonitorStarted() {
        sendMessage((int) EVENT_CONNECTIVITY_MONITOR_STARTED);
        return true;
    }

    public boolean sendEventConnectivityMonitorStartingFailure() {
        sendMessage((int) EVENT_CONNECTIVITY_MONITOR_STARTING_FAILURE);
        return true;
    }

    public boolean sendEventConnectivityMonitorStopped() {
        sendMessage((int) EVENT_CONNECTIVITY_MONITOR_STOPPED);
        return true;
    }

    public boolean sendEventPrerequisitesCheckFailure(String message) {
        MainService.sendEventServicesStartingFailure(this.mServiceAsyncChannel, message);
        return true;
    }

    public boolean sendEventPrerequisitesChecked() {
        sendMessage((int) EVENT_PREREQUISITES_CHECKED);
        return true;
    }

    public boolean isStarting() {
        Log.m3v(LOG_TAG, "isStarting getCurrentState=" + getCurrentState().toString());
        return getCurrentState().equals(this.mStartingState);
    }

    public boolean isStopping() {
        Log.m3v(LOG_TAG, "isStopping getCurrentState=" + getCurrentState().toString());
        return getCurrentState().equals(this.mStoppingState);
    }
}
