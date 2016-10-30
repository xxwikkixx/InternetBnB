package com.fattin.hotspot.main;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import com.fattin.hotspot.connectivity.HeartbeatTaskManager;
import com.fattin.hotspot.helpers.Log;
import com.fattin.hotspot.sm.State;
import com.fattin.hotspot.sm.StateMachine;

public class HeartbeatSchedulerStateMachine extends StateMachine {
    private static final int BASE = 413696;
    private static final int CMD_STARTING_HEARTBEAT_SCHEDULER = 413698;
    private static final int CMD_START_HEARTBEAT_SCHEDULER = 413697;
    private static final int CMD_STOPPING_HEARTBEAT_SCHEDULER = 413700;
    private static final int CMD_STOP_HEARTBEAT_SCHEDULER = 413699;
    private static final String LOG_TAG = "HeartbeatSchedulerStateMachine";
    private ConnectivityMonitorStateMachine mConnectivityMonitorStateMachine;
    private DefaultState mDefaultState;
    private StartedState mStartedState;
    private StoppedState mStoppedState;

    private class DefaultState extends State {
        private DefaultState() {
        }

        public void enter() {
            Log.m3v(HeartbeatSchedulerStateMachine.LOG_TAG, "DefaultState.enter");
        }

        public boolean processMessage(Message msg) {
            Log.m3v(HeartbeatSchedulerStateMachine.LOG_TAG, "DefaultState.processMessage " + msg.what);
            switch (msg.what) {
                case HeartbeatSchedulerStateMachine.CMD_START_HEARTBEAT_SCHEDULER /*413697*/:
                    HeartbeatSchedulerStateMachine.this.sendMessage((int) HeartbeatSchedulerStateMachine.CMD_STARTING_HEARTBEAT_SCHEDULER);
                    HeartbeatSchedulerStateMachine.this.transitionTo(HeartbeatSchedulerStateMachine.this.mStartedState);
                    break;
                case HeartbeatSchedulerStateMachine.CMD_STOP_HEARTBEAT_SCHEDULER /*413699*/:
                    HeartbeatSchedulerStateMachine.this.sendMessage((int) HeartbeatSchedulerStateMachine.CMD_STOPPING_HEARTBEAT_SCHEDULER);
                    HeartbeatSchedulerStateMachine.this.transitionTo(HeartbeatSchedulerStateMachine.this.mStoppedState);
                    break;
            }
            return true;
        }

        public void exit() {
            Log.m3v(HeartbeatSchedulerStateMachine.LOG_TAG, "DefaultState.exit");
        }
    }

    private class StartedState extends State {
        private StartedState() {
        }

        public void enter() {
            Log.m3v(HeartbeatSchedulerStateMachine.LOG_TAG, "StartedState.enter");
        }

        public boolean processMessage(Message msg) {
            Log.m3v(HeartbeatSchedulerStateMachine.LOG_TAG, "StartedState.processMessage " + msg.what);
            switch (msg.what) {
                case HeartbeatSchedulerStateMachine.CMD_START_HEARTBEAT_SCHEDULER /*413697*/:
                    HeartbeatSchedulerStateMachine.this.mConnectivityMonitorStateMachine.sendEventHearbeatSchedulerStarted();
                    break;
                case HeartbeatSchedulerStateMachine.CMD_STARTING_HEARTBEAT_SCHEDULER /*413698*/:
                    HeartbeatTaskManager.startHeartbeatTaskTimer();
                    HeartbeatSchedulerStateMachine.this.mConnectivityMonitorStateMachine.sendEventHearbeatSchedulerStarted();
                    break;
                default:
                    return false;
            }
            return true;
        }

        public void exit() {
            Log.m3v(HeartbeatSchedulerStateMachine.LOG_TAG, "StartedState.exit");
        }
    }

    private class StoppedState extends State {
        private StoppedState() {
        }

        public void enter() {
            Log.m3v(HeartbeatSchedulerStateMachine.LOG_TAG, "StoppedState.enter");
        }

        public boolean processMessage(Message msg) {
            Log.m3v(HeartbeatSchedulerStateMachine.LOG_TAG, "StoppedState.processMessage " + msg.what);
            switch (msg.what) {
                case HeartbeatSchedulerStateMachine.CMD_STOP_HEARTBEAT_SCHEDULER /*413699*/:
                case HeartbeatSchedulerStateMachine.CMD_STOPPING_HEARTBEAT_SCHEDULER /*413700*/:
                    HeartbeatTaskManager.stopHeartbeatTaskTimer();
                    HeartbeatSchedulerStateMachine.this.mConnectivityMonitorStateMachine.sendEventHearbeatSchedulerStopped();
                    return true;
                default:
                    return false;
            }
        }

        public void exit() {
            Log.m3v(HeartbeatSchedulerStateMachine.LOG_TAG, "StoppedState.exit");
        }
    }

    protected HeartbeatSchedulerStateMachine(Context context, ConnectivityMonitorStateMachine cmsm, Handler target) {
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

    public boolean sendCommandStartHeartbeatScheduler() {
        Log.m3v(LOG_TAG, "sendStartHeartbeatScheduler");
        sendMessage((int) CMD_START_HEARTBEAT_SCHEDULER);
        return true;
    }

    public boolean sendCommandStopHeartbeatScheduler() {
        Log.m3v(LOG_TAG, "sendStopHeartbeatScheduler");
        sendMessage((int) CMD_STOP_HEARTBEAT_SCHEDULER);
        return true;
    }
}
