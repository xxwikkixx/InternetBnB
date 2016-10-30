package com.fattin.hotspot.connectivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Message;
import com.fattin.hotspot.app.GlobalStates;
import com.fattin.hotspot.connectivity.ConnectivityMonitorManager.State;
import com.fattin.hotspot.helpers.Log;
import com.fattin.hotspot.netfilter.NetFilterManager;
import java.util.ArrayList;
import java.util.Iterator;

public class ConnectivityBroadcastReceiver extends BroadcastReceiver {
    public static final String LOG_TAG = "ConnectivityBroadcastReceiver -> ";

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
                    NetFilterManager.enableNatRules(true);
                    ConnectivityMonitorManager.setWiFiTethered(true);
                    return;
                }
            }
            ConnectivityMonitorManager.setWiFiTethered(false);
            NetFilterManager.disableNatRules(true);
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
                NetFilterManager.enableNatRules(true);
            }
            for (Handler target : ConnectivityMonitorManager.getHandlers().keySet()) {
                target.sendMessage(Message.obtain(target, ((Integer) ConnectivityMonitorManager.getHandlers().get(target)).intValue()));
            }
        }
    }
}
