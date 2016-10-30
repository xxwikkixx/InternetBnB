package com.fattin.hotspot.main;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Message;
import com.fattin.hotspot.app.GlobalStates;
import com.fattin.hotspot.app.MainApplication;
import com.fattin.hotspot.connectivity.ConnectivityMonitorManager;
import com.fattin.hotspot.connectivity.ConnectivityMonitorManager.State;
import com.fattin.hotspot.connectivity.TetherManager;
import com.fattin.hotspot.helpers.Log;
import com.fattin.hotspot.sm.StateMachine;
import java.util.ArrayList;
import java.util.Iterator;

public class ConnectivityMonitorStateMachine extends StateMachine {
    private static final int BASE = 421888;
    private static final int CMD_DISABLE_CONNECTIVITY_MONITOR = 421896;
    private static final int CMD_DISABLING_CONNECTIVITY_MONITOR = 421895;
    private static final int CMD_ENABLE_CONNECTIVITY_MONITOR = 421894;
    private static final int CMD_ENABLING_CONNECTIVITY_MONITOR = 421893;
    private static final int CMD_START_CONNECTIVITY_MONITOR = 421889;
    private static final int CMD_STOPPING_CONNECTIVITY_MONITOR = 421891;
    private static final int CMD_STOP_CONNECTIVITY_MONITOR = 421892;
    static final int EVENT_HEARTBEAT_SCHEDULER_STARTED = 421904;
    static final int EVENT_HEARTBEAT_SCHEDULER_STOPPED = 421905;
    static final int EVENT_NETFILTER_DISABLED = 421900;
    static final int EVENT_NETFILTER_ENABLED = 421899;
    static final int EVENT_NETFILTER_ENABLING_FAILURE = 421901;
    static final int EVENT_NETFILTER_MONITOR_STARTED = 421902;
    static final int EVENT_NETFILTER_MONITOR_STOPPED = 421903;
    private static final String LOG_TAG = "ConnectivityMonitorStateMachine";
    private ConnectivityBroadcastReceiver mConnectivityBroadcastReceiver;
    private Context mContext;
    private DefaultState mDefaultState;
    private DisabledState mDisabledState;
    private DisablingState mDisablingState;
    private EnabledState mEnabledState;
    private EnablingState mEnablingState;
    HeartbeatSchedulerStateMachine mHeartbeatSchedulerStateMachine;
    private MainStateMachine mMainStateMachine;
    NetFilterMonitorStateMachine mNetFilterMonitorStateMachine;
    NetFilterStateMachine mNetFilterStateMachine;
    private StartedState mStartedState;
    private StoppedState mStoppedState;
    private StoppingState mStoppingState;

    private class ConnectivityBroadcastReceiver extends BroadcastReceiver {
        public static final String LOG_TAG = "ConnectivityBroadcastReceiver";

        private ConnectivityBroadcastReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if ((!action.equals("android.net.conn.CONNECTIVITY_CHANGE") && !action.equals(ConnectivityMonitorManager.ACTION_TETHER_STATE_CHANGED)) || !GlobalStates.isConnectivityListenerStarted()) {
                Log.m0d(LOG_TAG, "onReceived() called with " + ConnectivityMonitorManager.getState().toString() + " and " + intent);
            } else if (action.equals(ConnectivityMonitorManager.ACTION_TETHER_STATE_CHANGED)) {
                ArrayList<String> availableList = intent.getStringArrayListExtra(ConnectivityMonitorManager.EXTRA_AVAILABLE_TETHER);
                ArrayList<String> activeList = intent.getStringArrayListExtra(ConnectivityMonitorManager.EXTRA_ACTIVE_TETHER);
                ArrayList<String> erroredList = intent.getStringArrayListExtra(ConnectivityMonitorManager.EXTRA_ERRORED_TETHER);
                Log.m0d(LOG_TAG, "onReceive():  availableList sizes=" + availableList.size() + ", " + activeList.size() + ", " + erroredList.size());
                Log.m0d(LOG_TAG, "onReceive(): availableList=" + availableList + " activeList= " + activeList + " erroredList=" + erroredList);
                Iterator it = activeList.iterator();
                while (it.hasNext()) {
                    String iface = (String) it.next();
                    if (iface.contains(TetherManager.getSingleton().getTetheredWifiIface())) {
                        Log.m0d(LOG_TAG, "onReceive(): activeList= " + activeList + " containts " + iface);
                    }
                }
                it = activeList.iterator();
                while (it.hasNext()) {
                    if (((String) it.next()).contains(TetherManager.getSingleton().getTetheredWifiIface())) {
                        ConnectivityMonitorStateMachine.this.sendCommandEnableConnectivityMonitor();
                        ConnectivityMonitorManager.setWiFiTethered(true);
                        return;
                    }
                }
                ConnectivityMonitorManager.setWiFiTethered(false);
                ConnectivityMonitorStateMachine.this.sendCommandDisableConnectivityMonitor();
            } else if (action.equals("android.net.conn.CONNECTIVITY_CHANGE")) {
                String str;
                boolean noConnectivity = intent.getBooleanExtra("noConnectivity", false);
                if (noConnectivity) {
                    ConnectivityMonitorManager.setState(State.NOT_CONNECTED);
                } else {
                    ConnectivityMonitorManager.setState(State.CONNECTED);
                }
                ConnectivityMonitorManager.setNetworkInfo((NetworkInfo) intent.getParcelableExtra("networkInfo"));
                ConnectivityMonitorManager.setOtherNetworkInfo((NetworkInfo) intent.getParcelableExtra("otherNetwork"));
                ConnectivityMonitorManager.setReason(intent.getStringExtra("reason"));
                ConnectivityMonitorManager.setFailover(intent.getBooleanExtra("isFailover", false));
                String str2 = LOG_TAG;
                StringBuilder append = new StringBuilder("onReceive(): mNetworkInfo=").append(ConnectivityMonitorManager.getNetworkInfo()).append(" mOtherNetworkInfo = ");
                if (ConnectivityMonitorManager.getOtherNetworkInfo() == null) {
                    str = "[none]";
                } else {
                    str = ConnectivityMonitorManager.getOtherNetworkInfo() + " noConn=" + noConnectivity;
                }
                Log.m0d(str2, append.append(str).append(" mState=").append(ConnectivityMonitorManager.getState().toString()).toString());
                if (ConnectivityMonitorManager.isWiFiTethered() && ConnectivityMonitorManager.getNetworkInfo().isAvailable()) {
                    ConnectivityMonitorStateMachine.this.sendCommandEnableConnectivityMonitor();
                }
            }
        }
    }

    private class DefaultState extends com.fattin.hotspot.sm.State {
        private DefaultState() {
        }

        public void enter() {
            Log.m3v(ConnectivityMonitorStateMachine.LOG_TAG, "DefaultState.enter");
        }

        public boolean processMessage(Message msg) {
            Log.m3v(ConnectivityMonitorStateMachine.LOG_TAG, "DefaultState.processMessage " + msg.what);
            switch (msg.what) {
                case ConnectivityMonitorStateMachine.CMD_START_CONNECTIVITY_MONITOR /*421889*/:
                    ConnectivityMonitorStateMachine.this.startConnectivityMonitor();
                    ConnectivityMonitorStateMachine.this.sendMessage((int) ConnectivityMonitorStateMachine.CMD_ENABLE_CONNECTIVITY_MONITOR);
                    ConnectivityMonitorStateMachine.this.transitionTo(ConnectivityMonitorStateMachine.this.mStartedState);
                    break;
                case ConnectivityMonitorStateMachine.CMD_STOP_CONNECTIVITY_MONITOR /*421892*/:
                    ConnectivityMonitorStateMachine.this.stopConnectivityMonitor();
                    ConnectivityMonitorStateMachine.this.sendMessage((int) ConnectivityMonitorStateMachine.CMD_STOPPING_CONNECTIVITY_MONITOR);
                    ConnectivityMonitorStateMachine.this.transitionTo(ConnectivityMonitorStateMachine.this.mStoppingState);
                    break;
            }
            return true;
        }

        public void exit() {
            Log.m3v(ConnectivityMonitorStateMachine.LOG_TAG, "DefaultState.exit");
        }
    }

    private class DisabledState extends com.fattin.hotspot.sm.State {
        private DisabledState() {
        }

        public void enter() {
            Log.m3v(ConnectivityMonitorStateMachine.LOG_TAG, "DisabledState.enter");
        }

        public boolean processMessage(Message msg) {
            Log.m3v(ConnectivityMonitorStateMachine.LOG_TAG, "DisabledState.processMessage " + msg.what);
            switch (msg.what) {
                case ConnectivityMonitorStateMachine.CMD_DISABLING_CONNECTIVITY_MONITOR /*421895*/:
                case ConnectivityMonitorStateMachine.CMD_DISABLE_CONNECTIVITY_MONITOR /*421896*/:
                    return true;
                default:
                    return false;
            }
        }

        public void exit() {
            Log.m3v(ConnectivityMonitorStateMachine.LOG_TAG, "DisabledState.exit");
        }
    }

    private class DisablingState extends com.fattin.hotspot.sm.State {
        private DisablingState() {
        }

        public void enter() {
            Log.m3v(ConnectivityMonitorStateMachine.LOG_TAG, "DisablingState.enter");
        }

        public boolean processMessage(Message msg) {
            Log.m3v(ConnectivityMonitorStateMachine.LOG_TAG, "DisablingState.processMessage " + msg.what);
            switch (msg.what) {
                case ConnectivityMonitorStateMachine.CMD_START_CONNECTIVITY_MONITOR /*421889*/:
                    ConnectivityMonitorStateMachine.this.mMainStateMachine.sendEventConnectivityMonitorStarted();
                    break;
                case ConnectivityMonitorStateMachine.CMD_DISABLING_CONNECTIVITY_MONITOR /*421895*/:
                    ConnectivityMonitorStateMachine.this.mNetFilterMonitorStateMachine.sendCommandStopNetFilterMonitor();
                    break;
                case ConnectivityMonitorStateMachine.CMD_DISABLE_CONNECTIVITY_MONITOR /*421896*/:
                    break;
                case ConnectivityMonitorStateMachine.EVENT_NETFILTER_DISABLED /*421900*/:
                    ConnectivityMonitorStateMachine.this.sendMessage((int) ConnectivityMonitorStateMachine.CMD_DISABLE_CONNECTIVITY_MONITOR);
                    ConnectivityMonitorStateMachine.this.transitionTo(ConnectivityMonitorStateMachine.this.mDisabledState);
                    break;
                case ConnectivityMonitorStateMachine.EVENT_NETFILTER_MONITOR_STOPPED /*421903*/:
                    ConnectivityMonitorStateMachine.this.mHeartbeatSchedulerStateMachine.sendCommandStopHeartbeatScheduler();
                    break;
                case ConnectivityMonitorStateMachine.EVENT_HEARTBEAT_SCHEDULER_STOPPED /*421905*/:
                    ConnectivityMonitorStateMachine.this.mNetFilterStateMachine.sendCommandDisableNetfilter();
                    break;
                default:
                    return false;
            }
            return true;
        }

        public void exit() {
            Log.m3v(ConnectivityMonitorStateMachine.LOG_TAG, "DisablingState.exit");
        }
    }

    private class EnabledState extends com.fattin.hotspot.sm.State {
        private EnabledState() {
        }

        public void enter() {
            Log.m3v(ConnectivityMonitorStateMachine.LOG_TAG, "EnabledState.enter");
        }

        public boolean processMessage(Message msg) {
            Log.m3v(ConnectivityMonitorStateMachine.LOG_TAG, "EnabledState.processMessage " + msg.what);
            switch (msg.what) {
                case ConnectivityMonitorStateMachine.CMD_START_CONNECTIVITY_MONITOR /*421889*/:
                case ConnectivityMonitorStateMachine.CMD_ENABLING_CONNECTIVITY_MONITOR /*421893*/:
                case ConnectivityMonitorStateMachine.CMD_ENABLE_CONNECTIVITY_MONITOR /*421894*/:
                    ConnectivityMonitorStateMachine.this.mMainStateMachine.sendEventConnectivityMonitorStarted();
                    return true;
                default:
                    return false;
            }
        }

        public void exit() {
            Log.m3v(ConnectivityMonitorStateMachine.LOG_TAG, "EnabledState.exit");
        }
    }

    private class EnablingState extends com.fattin.hotspot.sm.State {
        private EnablingState() {
        }

        public void enter() {
            Log.m3v(ConnectivityMonitorStateMachine.LOG_TAG, "EnablingState.enter");
        }

        public boolean processMessage(Message msg) {
            Log.m3v(ConnectivityMonitorStateMachine.LOG_TAG, "EnablingState.processMessage " + msg.what);
            switch (msg.what) {
                case ConnectivityMonitorStateMachine.CMD_START_CONNECTIVITY_MONITOR /*421889*/:
                case ConnectivityMonitorStateMachine.CMD_ENABLE_CONNECTIVITY_MONITOR /*421894*/:
                    break;
                case ConnectivityMonitorStateMachine.CMD_ENABLING_CONNECTIVITY_MONITOR /*421893*/:
                    ConnectivityMonitorStateMachine.this.mNetFilterStateMachine.sendCommandEnableNetfilter();
                    break;
                case ConnectivityMonitorStateMachine.EVENT_NETFILTER_ENABLED /*421899*/:
                    ConnectivityMonitorStateMachine.this.mNetFilterMonitorStateMachine.sendCommandStartNetFilterMonitor();
                    break;
                case ConnectivityMonitorStateMachine.EVENT_NETFILTER_ENABLING_FAILURE /*421901*/:
                    ConnectivityMonitorStateMachine.this.mMainStateMachine.sendEventConnectivityMonitorStartingFailure();
                    break;
                case ConnectivityMonitorStateMachine.EVENT_NETFILTER_MONITOR_STARTED /*421902*/:
                    ConnectivityMonitorStateMachine.this.mHeartbeatSchedulerStateMachine.sendCommandStartHeartbeatScheduler();
                    break;
                case ConnectivityMonitorStateMachine.EVENT_HEARTBEAT_SCHEDULER_STARTED /*421904*/:
                    ConnectivityMonitorStateMachine.this.sendMessage((int) ConnectivityMonitorStateMachine.CMD_ENABLE_CONNECTIVITY_MONITOR);
                    ConnectivityMonitorStateMachine.this.transitionTo(ConnectivityMonitorStateMachine.this.mEnabledState);
                    break;
                default:
                    return false;
            }
            return true;
        }

        public void exit() {
            Log.m3v(ConnectivityMonitorStateMachine.LOG_TAG, "EnablingState.exit");
        }
    }

    private class StartedState extends com.fattin.hotspot.sm.State {
        private StartedState() {
        }

        public void enter() {
            Log.m3v(ConnectivityMonitorStateMachine.LOG_TAG, "StartedState.enter");
        }

        public boolean processMessage(Message msg) {
            Log.m3v(ConnectivityMonitorStateMachine.LOG_TAG, "StartedState.processMessage " + msg.what);
            switch (msg.what) {
                case ConnectivityMonitorStateMachine.CMD_START_CONNECTIVITY_MONITOR /*421889*/:
                    break;
                case ConnectivityMonitorStateMachine.CMD_ENABLE_CONNECTIVITY_MONITOR /*421894*/:
                    ConnectivityMonitorStateMachine.this.sendMessage((int) ConnectivityMonitorStateMachine.CMD_ENABLING_CONNECTIVITY_MONITOR);
                    ConnectivityMonitorStateMachine.this.transitionTo(ConnectivityMonitorStateMachine.this.mEnablingState);
                    break;
                case ConnectivityMonitorStateMachine.CMD_DISABLE_CONNECTIVITY_MONITOR /*421896*/:
                    ConnectivityMonitorStateMachine.this.sendMessage((int) ConnectivityMonitorStateMachine.CMD_DISABLING_CONNECTIVITY_MONITOR);
                    ConnectivityMonitorStateMachine.this.transitionTo(ConnectivityMonitorStateMachine.this.mDisablingState);
                    break;
                default:
                    return false;
            }
            return true;
        }

        public void exit() {
            Log.m3v(ConnectivityMonitorStateMachine.LOG_TAG, "StartedState.exit");
        }
    }

    private class StoppedState extends com.fattin.hotspot.sm.State {
        private StoppedState() {
        }

        public void enter() {
            Log.m3v(ConnectivityMonitorStateMachine.LOG_TAG, "StoppedState.enter");
        }

        public boolean processMessage(Message msg) {
            Log.m3v(ConnectivityMonitorStateMachine.LOG_TAG, "StoppedState.processMessage " + msg.what);
            switch (msg.what) {
                case ConnectivityMonitorStateMachine.CMD_STOPPING_CONNECTIVITY_MONITOR /*421891*/:
                case ConnectivityMonitorStateMachine.CMD_STOP_CONNECTIVITY_MONITOR /*421892*/:
                    ConnectivityMonitorStateMachine.this.mMainStateMachine.sendEventConnectivityMonitorStopped();
                    return true;
                default:
                    return false;
            }
        }

        public void exit() {
            Log.m3v(ConnectivityMonitorStateMachine.LOG_TAG, "StoppedState.exit");
        }
    }

    private class StoppingState extends com.fattin.hotspot.sm.State {
        private StoppingState() {
        }

        public void enter() {
            Log.m3v(ConnectivityMonitorStateMachine.LOG_TAG, "StoppingState.enter");
        }

        public boolean processMessage(Message msg) {
            Log.m3v(ConnectivityMonitorStateMachine.LOG_TAG, "StoppingState.processMessage " + msg.what);
            switch (msg.what) {
                case ConnectivityMonitorStateMachine.CMD_STOPPING_CONNECTIVITY_MONITOR /*421891*/:
                    ConnectivityMonitorStateMachine.this.mNetFilterMonitorStateMachine.sendCommandStopNetFilterMonitor();
                    break;
                case ConnectivityMonitorStateMachine.CMD_STOP_CONNECTIVITY_MONITOR /*421892*/:
                    break;
                case ConnectivityMonitorStateMachine.EVENT_NETFILTER_DISABLED /*421900*/:
                    ConnectivityMonitorStateMachine.this.sendMessage((int) ConnectivityMonitorStateMachine.CMD_STOP_CONNECTIVITY_MONITOR);
                    ConnectivityMonitorStateMachine.this.transitionTo(ConnectivityMonitorStateMachine.this.mStoppedState);
                    break;
                case ConnectivityMonitorStateMachine.EVENT_NETFILTER_MONITOR_STOPPED /*421903*/:
                    ConnectivityMonitorStateMachine.this.mHeartbeatSchedulerStateMachine.sendCommandStopHeartbeatScheduler();
                    break;
                case ConnectivityMonitorStateMachine.EVENT_HEARTBEAT_SCHEDULER_STOPPED /*421905*/:
                    ConnectivityMonitorStateMachine.this.mNetFilterStateMachine.sendCommandDisableNetfilter();
                    break;
                default:
                    return false;
            }
            return true;
        }

        public void exit() {
            Log.m3v(ConnectivityMonitorStateMachine.LOG_TAG, "StoppingState.exit");
        }
    }

    public ConnectivityMonitorStateMachine(Context context, MainStateMachine msm, Handler target) {
        super(LOG_TAG, target.getLooper());
        this.mDefaultState = new DefaultState();
        this.mStartedState = new StartedState();
        this.mStoppingState = new StoppingState();
        this.mStoppedState = new StoppedState();
        this.mEnablingState = new EnablingState();
        this.mEnabledState = new EnabledState();
        this.mDisablingState = new DisablingState();
        this.mDisabledState = new DisabledState();
        this.mContext = context;
        this.mMainStateMachine = msm;
        addState(this.mDefaultState);
        addState(this.mStartedState, this.mDefaultState);
        addState(this.mEnablingState, this.mStartedState);
        addState(this.mEnabledState, this.mEnablingState);
        addState(this.mDisablingState, this.mStartedState);
        addState(this.mDisabledState, this.mDisablingState);
        addState(this.mStoppingState, this.mDefaultState);
        addState(this.mStoppedState, this.mStoppingState);
        setInitialState(this.mStoppedState);
        start();
        this.mNetFilterStateMachine = new NetFilterStateMachine(this.mContext, this, getHandler());
        this.mNetFilterMonitorStateMachine = new NetFilterMonitorStateMachine(this.mContext, this, getHandler());
        this.mHeartbeatSchedulerStateMachine = new HeartbeatSchedulerStateMachine(this.mContext, this, getHandler());
    }

    public void startConnectivityMonitor() {
        ConnectivityMonitorManager.startListening(MainApplication.getAppContext(), getConnectivityReceiver());
    }

    public void stopConnectivityMonitor() {
        ConnectivityMonitorManager.stopListening();
    }

    public boolean sendCommandStartConnectivityMonitor() {
        Log.m3v(LOG_TAG, "sendCommandStartConnectivityMonitor");
        sendMessage((int) CMD_START_CONNECTIVITY_MONITOR);
        return true;
    }

    public boolean sendCommandStopConnectivityMonitor() {
        Log.m3v(LOG_TAG, "sendCommandStopConnectivityMonitor");
        sendMessage((int) CMD_STOP_CONNECTIVITY_MONITOR);
        return true;
    }

    private boolean sendCommandEnableConnectivityMonitor() {
        Log.m3v(LOG_TAG, "sendCommandEnableConnectivityMonitor");
        sendMessage((int) CMD_ENABLE_CONNECTIVITY_MONITOR);
        return true;
    }

    private boolean sendCommandDisableConnectivityMonitor() {
        Log.m3v(LOG_TAG, "sendCommandDisableConnectivityMonitor");
        sendMessage((int) CMD_DISABLE_CONNECTIVITY_MONITOR);
        return true;
    }

    public boolean sendEventNetFilterEnabled() {
        sendMessage((int) EVENT_NETFILTER_ENABLED);
        return true;
    }

    public boolean sendEventNetFilterEnablingFailure() {
        sendMessage((int) EVENT_NETFILTER_ENABLING_FAILURE);
        return true;
    }

    public boolean sendEventNetFilterDisabled() {
        sendMessage((int) EVENT_NETFILTER_DISABLED);
        return true;
    }

    public boolean sendEventNetFilterMonitorStarted() {
        sendMessage((int) EVENT_NETFILTER_MONITOR_STARTED);
        return true;
    }

    public boolean sendEventNetFilterMonitorStopped() {
        sendMessage((int) EVENT_NETFILTER_MONITOR_STOPPED);
        return true;
    }

    public boolean sendEventHearbeatSchedulerStarted() {
        sendMessage((int) EVENT_HEARTBEAT_SCHEDULER_STARTED);
        return true;
    }

    public boolean sendEventHearbeatSchedulerStopped() {
        sendMessage((int) EVENT_HEARTBEAT_SCHEDULER_STOPPED);
        return true;
    }

    private BroadcastReceiver getConnectivityReceiver() {
        if (this.mConnectivityBroadcastReceiver == null) {
            this.mConnectivityBroadcastReceiver = new ConnectivityBroadcastReceiver();
        }
        return this.mConnectivityBroadcastReceiver;
    }
}
