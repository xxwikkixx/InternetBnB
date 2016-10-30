package com.fattin.hotspot.connectivity;

import com.fattin.hotspot.app.Constants;
import com.fattin.hotspot.app.GlobalStates;
import com.fattin.hotspot.helpers.Log;
import com.fattin.hotspot.netfilter.NetFilterManager;
import java.util.TimerTask;

public class NetfilterMonitorTask extends TimerTask implements Runnable {
    private static final String LOG_TAG = "NetfilterMonitorTask";
    private static long counter;

    static {
        counter = 0;
    }

    public void run() {
        Log.m0d(LOG_TAG, "run");
        if (!GlobalStates.isNatMonitorStarted()) {
            counter = 0;
        } else if (!NetFilterManager.isNatTablesValid()) {
            Log.m0d(LOG_TAG, "run -> nat table is not valid!");
            NetFilterManager.rebuildNatRules();
            counter = 0;
        } else if (counter >= Constants.DNS_MONITOR_INTERVAL) {
            Log.m0d(LOG_TAG, "run -> updating iptables whitelist!");
            NetFilterManager.updateWhiteList();
            counter = 0;
        } else {
            counter += Constants.NETFILTER_MONITOR_INTERVAL;
        }
    }
}
