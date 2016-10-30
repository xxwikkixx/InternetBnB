package com.fattin.hotspot.connectivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.os.Handler;
import com.fattin.hotspot.app.GlobalStates;
import com.fattin.hotspot.helpers.Log;
import com.fattin.hotspot.netfilter.NetFilterManager;
import java.util.HashMap;

public class ConnectivityMonitorManager {
    public static final String ACTION_TETHER_STATE_CHANGED = "android.net.conn.TETHER_STATE_CHANGED";
    public static final String EXTRA_ACTIVE_TETHER = "activeArray";
    public static final String EXTRA_AVAILABLE_TETHER = "availableArray";
    public static final String EXTRA_ERRORED_TETHER = "erroredArray";
    public static final String LOG_TAG = "ConnectivityMonitorManager";
    private static State connectivityState;
    private static Context mContext;
    private static HashMap<Handler, Integer> mHandlers;
    private static boolean mIsFailover;
    private static boolean mIsWiFiTethered;
    private static NetworkInfo mNetworkInfo;
    private static NetworkInfo mOtherNetworkInfo;
    private static String mReason;
    private static BroadcastReceiver mReceiver;

    public enum State {
        UNKNOWN,
        CONNECTED,
        NOT_CONNECTED
    }

    static {
        mHandlers = new HashMap();
        connectivityState = State.UNKNOWN;
        mIsWiFiTethered = false;
        mContext = null;
        mReceiver = null;
    }

    public static synchronized boolean startListening(Context context, BroadcastReceiver receiver) {
        boolean z = true;
        synchronized (ConnectivityMonitorManager.class) {
            if (mContext == null) {
                mContext = context;
            }
            if (receiver == null && mReceiver == null) {
                z = false;
            } else if (!GlobalStates.isConnectivityListenerStarted()) {
                if (receiver != null) {
                    mReceiver = receiver;
                }
                GlobalStates.setConnectivityListenerStarted(true);
                Log.m3v(LOG_TAG, "startListening");
                IntentFilter filter = new IntentFilter();
                filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
                filter.addAction(ACTION_TETHER_STATE_CHANGED);
                Log.m0d(LOG_TAG, receiver.toString());
                mContext.registerReceiver(receiver, filter);
            }
        }
        return z;
    }

    public static synchronized void stopListening() {
        synchronized (ConnectivityMonitorManager.class) {
            NetFilterManager.disableNatRules(false);
            if (GlobalStates.isConnectivityListenerStarted()) {
                Log.m3v(LOG_TAG, "stopListening");
                mContext.unregisterReceiver(mReceiver);
                mReceiver = null;
                mNetworkInfo = null;
                mOtherNetworkInfo = null;
                mIsFailover = false;
                mReason = null;
                GlobalStates.setConnectivityListenerStarted(false);
            }
        }
    }

    public static void registerHandler(Handler target, int what) {
        mHandlers.put(target, Integer.valueOf(what));
    }

    public static void unregisterHandler(Handler target) {
        mHandlers.remove(target);
    }

    public static HashMap<Handler, Integer> getHandlers() {
        return mHandlers;
    }

    public static State getState() {
        return connectivityState;
    }

    public static void setState(State state) {
        connectivityState = state;
    }

    public static NetworkInfo getNetworkInfo() {
        return mNetworkInfo;
    }

    public static void setNetworkInfo(NetworkInfo networkInfo) {
        mNetworkInfo = networkInfo;
    }

    public static NetworkInfo getOtherNetworkInfo() {
        return mOtherNetworkInfo;
    }

    public static void setOtherNetworkInfo(NetworkInfo networkInfo) {
        mOtherNetworkInfo = networkInfo;
    }

    public static boolean isFailover() {
        return mIsFailover;
    }

    public static void setFailover(boolean isFailover) {
        mIsFailover = isFailover;
    }

    public static boolean isWiFiTethered() {
        return mIsWiFiTethered;
    }

    public static void setWiFiTethered(boolean isWiFiTethered) {
        mIsWiFiTethered = isWiFiTethered;
    }

    public static String getReason() {
        return mReason;
    }

    public static void setReason(String reason) {
        mReason = reason;
    }
}
