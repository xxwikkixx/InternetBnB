package com.fattin.hotspot.main;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import com.fattin.hotspot.helpers.Log;
import com.fattin.hotspot.netfilter.NetFilterManager;
import com.fattin.hotspot.sm.State;
import com.fattin.hotspot.sm.StateMachine;

public class NetFilterStateMachine extends StateMachine {
    private static final int BASE = 405504;
    private static final int CMD_DISABLE_NETFILTER = 405508;
    private static final int CMD_DISABLING_NETFILTER = 405507;
    private static final int CMD_ENABLE_NETFILTER = 405506;
    private static final int CMD_ENABLING_NETFILTER = 405505;
    private static final String LOG_TAG = "NetFilterStateMachine";
    private ConnectivityMonitorStateMachine mConnectivityMonitorStateMachine;
    private DefaultState mDefaultState;
    private DisabledState mDisabledState;
    private DisablingState mDisablingState;
    private EnabledState mEnabledState;
    private EnablingState mEnablingState;

    private class DefaultState extends State {
        private DefaultState() {
        }

        public void enter() {
            Log.m3v(NetFilterStateMachine.LOG_TAG, "DefaultState.enter");
        }

        public boolean processMessage(Message msg) {
            Log.m3v(NetFilterStateMachine.LOG_TAG, "DefaultState.processMessage " + msg.what);
            switch (msg.what) {
                case NetFilterStateMachine.CMD_ENABLE_NETFILTER /*405506*/:
                    NetFilterStateMachine.this.sendMessage((int) NetFilterStateMachine.CMD_ENABLING_NETFILTER);
                    NetFilterStateMachine.this.transitionTo(NetFilterStateMachine.this.mEnablingState);
                    break;
                case NetFilterStateMachine.CMD_DISABLE_NETFILTER /*405508*/:
                    NetFilterStateMachine.this.sendMessage((int) NetFilterStateMachine.CMD_DISABLING_NETFILTER);
                    NetFilterStateMachine.this.transitionTo(NetFilterStateMachine.this.mDisablingState);
                    break;
            }
            return true;
        }

        public void exit() {
            Log.m3v(NetFilterStateMachine.LOG_TAG, "DefaultState.exit");
        }
    }

    private class DisabledState extends State {
        private DisabledState() {
        }

        public void enter() {
            Log.m3v(NetFilterStateMachine.LOG_TAG, "DisabledState.enter");
        }

        public boolean processMessage(Message msg) {
            Log.m3v(NetFilterStateMachine.LOG_TAG, "DisabledState.processMessage " + msg.what);
            switch (msg.what) {
                case NetFilterStateMachine.CMD_DISABLING_NETFILTER /*405507*/:
                case NetFilterStateMachine.CMD_DISABLE_NETFILTER /*405508*/:
                    NetFilterStateMachine.this.mConnectivityMonitorStateMachine.sendEventNetFilterDisabled();
                    return true;
                default:
                    return false;
            }
        }

        public void exit() {
            Log.m3v(NetFilterStateMachine.LOG_TAG, "DisabledState.exit");
        }
    }

    private class DisablingState extends State {
        private DisablingState() {
        }

        public void enter() {
            Log.m3v(NetFilterStateMachine.LOG_TAG, "DisablingState.enter");
        }

        public boolean processMessage(Message msg) {
            Log.m3v(NetFilterStateMachine.LOG_TAG, "DisablingState.processMessage " + msg.what);
            switch (msg.what) {
                case NetFilterStateMachine.CMD_DISABLING_NETFILTER /*405507*/:
                    NetFilterManager.disableNatRules(false);
                    NetFilterStateMachine.this.sendMessage((int) NetFilterStateMachine.CMD_DISABLE_NETFILTER);
                    NetFilterStateMachine.this.transitionTo(NetFilterStateMachine.this.mDisabledState);
                    break;
                case NetFilterStateMachine.CMD_DISABLE_NETFILTER /*405508*/:
                    break;
                default:
                    return false;
            }
            return true;
        }

        public void exit() {
            Log.m3v(NetFilterStateMachine.LOG_TAG, "DisablingState.exit");
        }
    }

    private class EnabledState extends State {
        private EnabledState() {
        }

        public void enter() {
            Log.m3v(NetFilterStateMachine.LOG_TAG, "EnabledState.enter");
        }

        public boolean processMessage(Message msg) {
            Log.m3v(NetFilterStateMachine.LOG_TAG, "EnabledState.processMessage " + msg.what);
            switch (msg.what) {
                case NetFilterStateMachine.CMD_ENABLING_NETFILTER /*405505*/:
                case NetFilterStateMachine.CMD_ENABLE_NETFILTER /*405506*/:
                    NetFilterStateMachine.this.mConnectivityMonitorStateMachine.sendEventNetFilterEnabled();
                    return true;
                default:
                    return false;
            }
        }

        public void exit() {
            Log.m3v(NetFilterStateMachine.LOG_TAG, "EnabledState.exit");
        }
    }

    private class EnablingState extends State {
        private EnablingState() {
        }

        public void enter() {
            Log.m3v(NetFilterStateMachine.LOG_TAG, "EnablingState.enter");
        }

        public boolean processMessage(Message msg) {
            Log.m3v(NetFilterStateMachine.LOG_TAG, "EnablingState.processMessage " + msg.what);
            switch (msg.what) {
                case NetFilterStateMachine.CMD_ENABLING_NETFILTER /*405505*/:
                    if (!NetFilterManager.enableNatRules(false)) {
                        NetFilterStateMachine.this.mConnectivityMonitorStateMachine.sendEventNetFilterEnablingFailure();
                        break;
                    }
                    NetFilterStateMachine.this.sendMessage((int) NetFilterStateMachine.CMD_ENABLE_NETFILTER);
                    NetFilterStateMachine.this.transitionTo(NetFilterStateMachine.this.mEnabledState);
                    break;
                case NetFilterStateMachine.CMD_ENABLE_NETFILTER /*405506*/:
                    break;
                default:
                    return false;
            }
            return true;
        }

        public void exit() {
            Log.m3v(NetFilterStateMachine.LOG_TAG, "EnablingState.exit");
        }
    }

    protected NetFilterStateMachine(Context context, ConnectivityMonitorStateMachine cmsm, Handler target) {
        super(LOG_TAG, target.getLooper());
        this.mDefaultState = new DefaultState();
        this.mEnablingState = new EnablingState();
        this.mEnabledState = new EnabledState();
        this.mDisablingState = new DisablingState();
        this.mDisabledState = new DisabledState();
        this.mConnectivityMonitorStateMachine = cmsm;
        addState(this.mDefaultState);
        addState(this.mEnablingState, this.mDefaultState);
        addState(this.mEnabledState, this.mEnablingState);
        addState(this.mDisablingState, this.mDefaultState);
        addState(this.mDisabledState, this.mDisablingState);
        setInitialState(this.mDisabledState);
        start();
    }

    public boolean sendCommandEnableNetfilter() {
        Log.m3v(LOG_TAG, "sendCommandEnableNetfilter");
        sendMessage((int) CMD_ENABLE_NETFILTER);
        return true;
    }

    public boolean sendCommandDisableNetfilter() {
        Log.m3v(LOG_TAG, "sendCommandDisableNetfilter");
        sendMessage((int) CMD_DISABLE_NETFILTER);
        return true;
    }
}
