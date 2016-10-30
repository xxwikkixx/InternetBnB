package com.fattin.hotspot.app;

import com.fattin.hotspot.helpers.Log;
import java.util.Hashtable;
import java.util.Map;

public class GlobalStates {
    private static final String IS_CONNECTIVITY_LISTENER_STARTED = "isConnectivityListenerStarted";
    private static final String IS_HEARTBEAT_ENABLED = "isHeartbeatEnabled";
    private static final String IS_NAT_MONITOR_STARTED = "isNatMonitorStarted";
    private static final String IS_NETFILTER_ENABLED = "isNetFilterEnabled";
    private static final String IS_SERVICE_STARTED = "isServiceStarted";
    private static final String LOG_TAG = "GlobalStates";
    private static Map<String, Object> settings;

    public enum State {
        ENABLING,
        ENABLED,
        DISABLING,
        DISABLED,
        UNKNOWN
    }

    static {
        settings = new Hashtable();
    }

    public static boolean isServiceStartingOrStarted() {
        Log.m0d(LOG_TAG, "getServiceState");
        State state = settings.containsKey(IS_SERVICE_STARTED) ? (State) settings.get(IS_SERVICE_STARTED) : State.UNKNOWN;
        return state == State.ENABLING || state == State.ENABLED;
    }

    public static boolean isServiceStartingOrStopping() {
        Log.m0d(LOG_TAG, "getServiceState");
        State state = settings.containsKey(IS_SERVICE_STARTED) ? (State) settings.get(IS_SERVICE_STARTED) : State.UNKNOWN;
        return state == State.ENABLING || state == State.DISABLING;
    }

    public static boolean isServiceStarted() {
        Log.m0d(LOG_TAG, "getServiceState");
        return (settings.containsKey(IS_SERVICE_STARTED) ? (State) settings.get(IS_SERVICE_STARTED) : State.UNKNOWN) == State.ENABLED;
    }

    public static boolean isServiceStoppingOrStopped() {
        Log.m0d(LOG_TAG, "getServiceState");
        State state = settings.containsKey(IS_SERVICE_STARTED) ? (State) settings.get(IS_SERVICE_STARTED) : State.UNKNOWN;
        return state == State.DISABLING || state == State.DISABLED;
    }

    public static State getServiceState() {
        Log.m0d(LOG_TAG, "getServiceState");
        return settings.containsKey(IS_SERVICE_STARTED) ? (State) settings.get(IS_SERVICE_STARTED) : State.UNKNOWN;
    }

    public static synchronized void setServiceState(State state) {
        synchronized (GlobalStates.class) {
            Log.m0d(LOG_TAG, "setServiceState");
            try {
                settings.put(IS_SERVICE_STARTED, state);
            } catch (Exception e) {
                Log.m1e(LOG_TAG, e.getMessage(), e);
            }
        }
    }

    public static boolean isHeartbeatEnabled() {
        Log.m0d(LOG_TAG, IS_HEARTBEAT_ENABLED);
        return settings.containsKey(IS_HEARTBEAT_ENABLED) ? ((Boolean) settings.get(IS_HEARTBEAT_ENABLED)).booleanValue() : false;
    }

    public static synchronized void setHeartbeatEnabled(boolean isEnabled) {
        synchronized (GlobalStates.class) {
            Log.m0d(LOG_TAG, "getHeartbeatEnabled");
            try {
                settings.put(IS_HEARTBEAT_ENABLED, Boolean.valueOf(isEnabled));
            } catch (Exception e) {
                Log.m1e(LOG_TAG, e.getMessage(), e);
            }
        }
    }

    public static boolean isNatMonitorStarted() {
        Log.m0d(LOG_TAG, IS_NAT_MONITOR_STARTED);
        return settings.containsKey(IS_NAT_MONITOR_STARTED) ? ((Boolean) settings.get(IS_NAT_MONITOR_STARTED)).booleanValue() : false;
    }

    public static synchronized void setNatMonitorStarted(boolean isStarted) {
        synchronized (GlobalStates.class) {
            Log.m0d(LOG_TAG, "setNatMonitorStarted");
            try {
                settings.put(IS_NAT_MONITOR_STARTED, Boolean.valueOf(isStarted));
            } catch (Exception e) {
                Log.m1e(LOG_TAG, e.getMessage(), e);
            }
        }
    }

    public static boolean isConnectivityListenerStarted() {
        Log.m0d(LOG_TAG, IS_CONNECTIVITY_LISTENER_STARTED);
        return settings.containsKey(IS_CONNECTIVITY_LISTENER_STARTED) ? ((Boolean) settings.get(IS_CONNECTIVITY_LISTENER_STARTED)).booleanValue() : false;
    }

    public static synchronized void setConnectivityListenerStarted(boolean isStarted) {
        synchronized (GlobalStates.class) {
            Log.m0d(LOG_TAG, "setConnectivityListenerStarted");
            try {
                settings.put(IS_CONNECTIVITY_LISTENER_STARTED, Boolean.valueOf(isStarted));
            } catch (Exception e) {
                Log.m1e(LOG_TAG, e.getMessage(), e);
            }
        }
    }

    public static boolean isNetFilterEnabled() {
        Log.m0d(LOG_TAG, IS_NETFILTER_ENABLED);
        return settings.containsKey(IS_NETFILTER_ENABLED) ? ((Boolean) settings.get(IS_NETFILTER_ENABLED)).booleanValue() : false;
    }

    public static synchronized void setNetFilterEnabled(boolean isEnabled) {
        synchronized (GlobalStates.class) {
            Log.m0d(LOG_TAG, "setNetFilterEnabled");
            try {
                settings.put(IS_NETFILTER_ENABLED, Boolean.valueOf(isEnabled));
            } catch (Exception e) {
                Log.m1e(LOG_TAG, e.getMessage(), e);
            }
        }
    }
}
