package com.fattin.hotspot.main;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import com.fattin.hotspot.connectivity.NetfilterMonitorTaskManager;
import com.fattin.hotspot.helpers.Log;
import com.fattin.hotspot.sm.State;
import com.fattin.hotspot.sm.StateMachine;

public class NetFilterMonitorStateMachine extends StateMachine {
    private static final int BASE = 417792;
    private static final int CMD_STARTING_NETFILTER_MONITOR = 417793;
    private static final int CMD_START_NETFILTER_MONITOR = 417794;
    private static final int CMD_STOPPING_NETFILTER_MONITOR = 417795;
    private static final int CMD_STOP_NETFILTER_MONITOR = 417796;
    private static final String LOG_TAG = "NetFilterMonitorStateMachine";
    private ConnectivityMonitorStateMachine mConnectivityMonitorStateMachine;
    private DefaultState mDefaultState;
    private StartedState mStartedState;
    private StoppedState mStoppedState;

    private class DefaultState extends State {
        private DefaultState() {
        }

        public void enter() {
            Log.m3v(NetFilterMonitorStateMachine.LOG_TAG, "DefaultState.enter");
        }

        public boolean processMessage(Message msg) {
            Log.m3v(NetFilterMonitorStateMachine.LOG_TAG, "DefaultState.processMessage " + msg.what);
            switch (msg.what) {
                case NetFilterMonitorStateMachine.CMD_START_NETFILTER_MONITOR /*417794*/:
                    NetFilterMonitorStateMachine.this.sendMessage((int) NetFilterMonitorStateMachine.CMD_STARTING_NETFILTER_MONITOR);
                    NetFilterMonitorStateMachine.this.transitionTo(NetFilterMonitorStateMachine.this.mStartedState);
                    break;
                case NetFilterMonitorStateMachine.CMD_STOP_NETFILTER_MONITOR /*417796*/:
                    NetFilterMonitorStateMachine.this.sendMessage((int) NetFilterMonitorStateMachine.CMD_STOPPING_NETFILTER_MONITOR);
                    NetFilterMonitorStateMachine.this.transitionTo(NetFilterMonitorStateMachine.this.mStoppedState);
                    break;
            }
            return true;
        }

        public void exit() {
            Log.m3v(NetFilterMonitorStateMachine.LOG_TAG, "DefaultState.exit");
        }
    }

    private class StartedState extends State {
        private StartedState() {
        }

        public void enter() {
            Log.m3v(NetFilterMonitorStateMachine.LOG_TAG, "StartedState.enter");
        }

        public boolean processMessage(Message msg) {
            Log.m3v(NetFilterMonitorStateMachine.LOG_TAG, "StartedState.processMessage " + msg.what);
            switch (msg.what) {
                case NetFilterMonitorStateMachine.CMD_STARTING_NETFILTER_MONITOR /*417793*/:
                    NetfilterMonitorTaskManager.startNatMonitorTaskTimer();
                    NetFilterMonitorStateMachine.this.mConnectivityMonitorStateMachine.sendEventNetFilterMonitorStarted();
                    break;
                case NetFilterMonitorStateMachine.CMD_START_NETFILTER_MONITOR /*417794*/:
                    NetFilterMonitorStateMachine.this.mConnectivityMonitorStateMachine.sendEventNetFilterMonitorStarted();
                    break;
                default:
                    return false;
            }
            return true;
        }

        public void exit() {
            Log.m3v(NetFilterMonitorStateMachine.LOG_TAG, "StartedState.exit");
        }
    }

    private class StoppedState extends State {
        private StoppedState() {
        }

        public void enter() {
            Log.m3v(NetFilterMonitorStateMachine.LOG_TAG, "StoppedState.enter");
        }

        public boolean processMessage(Message msg) {
            Log.m3v(NetFilterMonitorStateMachine.LOG_TAG, "StoppedState.processMessage " + msg.what);
            switch (msg.what) {
                case NetFilterMonitorStateMachine.CMD_STOPPING_NETFILTER_MONITOR /*417795*/:
                case NetFilterMonitorStateMachine.CMD_STOP_NETFILTER_MONITOR /*417796*/:
                    NetfilterMonitorTaskManager.stopNatMonitorTaskTimer();
                    NetFilterMonitorStateMachine.this.mConnectivityMonitorStateMachine.sendEventNetFilterMonitorStopped();
                    return true;
                default:
                    return false;
            }
        }

        public void exit() {
            Log.m3v(NetFilterMonitorStateMachine.LOG_TAG, "StoppedState.exit");
        }
    }

    protected NetFilterMonitorStateMachine(Context context, ConnectivityMonitorStateMachine cmsm, Handler target) {
        super(LOG_TAG, target.getLooper());
        this.mDefaultState = new DefaultState();
        this.mStartedState = new StartedState();
        this.mStoppedState = new StoppedState();
        this.mConnectivityMonitorStateMachine = cmsm;
        addState(this.mDefaultState);
        addState(this.mStartedState, this.mDefaultState);
        addState(this.mStoppedState, this.mDefaultState);
        setInitialState(this.mStoppedState);
        start();
    }

    public boolean sendCommandStartNetFilterMonitor() {
        Log.m3v(LOG_TAG, "sendCommandStartNetFilterMonitor");
        sendMessage((int) CMD_START_NETFILTER_MONITOR);
        return true;
    }

    public boolean sendCommandStopNetFilterMonitor() {
        Log.m3v(LOG_TAG, "sendCommandStopNetFilterMonitor");
        sendMessage((int) CMD_STOP_NETFILTER_MONITOR);
        return true;
    }
}
