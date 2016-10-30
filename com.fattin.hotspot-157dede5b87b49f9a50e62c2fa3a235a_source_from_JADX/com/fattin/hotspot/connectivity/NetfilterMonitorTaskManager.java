package com.fattin.hotspot.connectivity;

import com.fattin.hotspot.app.Constants;
import com.fattin.hotspot.app.GlobalStates;
import com.fattin.hotspot.helpers.Log;
import java.util.Timer;

public class NetfilterMonitorTaskManager {
    public static final String LOG_TAG = "NetfilterMonitorTaskManager";
    private static Timer timer;

    public static synchronized void startNatMonitorTaskTimer() {
        synchronized (NetfilterMonitorTaskManager.class) {
            Log.m0d(LOG_TAG, "startNetfilterMonitorTaskTimer");
            if (!GlobalStates.isNatMonitorStarted()) {
                GlobalStates.setNatMonitorStarted(true);
                timer = new Timer(true);
                timer.scheduleAtFixedRate(new NetfilterMonitorTask(), Constants.NETFILTER_MONITOR_INTERVAL, Constants.NETFILTER_MONITOR_INTERVAL);
                Log.m0d(LOG_TAG, "startNetfilterMonitorTaskTimer -> Task has been scheduled in timer");
            }
        }
    }

    public static synchronized void stopNatMonitorTaskTimer() {
        synchronized (NetfilterMonitorTaskManager.class) {
            Log.m0d(LOG_TAG, "stopNetfilterMonitorTaskTimer");
            if (GlobalStates.isNatMonitorStarted()) {
                GlobalStates.setNatMonitorStarted(false);
                timer.cancel();
                Log.m0d(LOG_TAG, "startNetfilterMonitorTaskTimer -> task timer is cancelled");
            }
        }
    }
}
