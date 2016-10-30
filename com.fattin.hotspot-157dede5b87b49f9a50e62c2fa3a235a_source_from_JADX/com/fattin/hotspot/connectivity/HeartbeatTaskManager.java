package com.fattin.hotspot.connectivity;

import com.fattin.hotspot.app.Constants;
import com.fattin.hotspot.app.GlobalStates;
import com.fattin.hotspot.helpers.Log;
import java.util.Timer;

public class HeartbeatTaskManager {
    public static final String LOG_TAG = "HeartbeatTaskManager";
    private static Timer timer;

    static {
        timer = new Timer(true);
    }

    public static synchronized void startHeartbeatTaskTimer() {
        synchronized (HeartbeatTaskManager.class) {
            Log.m0d(LOG_TAG, "startHeartbeatTaskTimer");
            if (!GlobalStates.isHeartbeatEnabled()) {
                GlobalStates.setHeartbeatEnabled(true);
                timer = new Timer(true);
                timer.scheduleAtFixedRate(new HeartbeatTask(), Constants.HEARTBEAT_INTERVAL, Constants.HEARTBEAT_INTERVAL);
                Log.m0d(LOG_TAG, "startHeartbeatTaskTimer -> Task has been scheduled in timer");
            }
        }
    }

    public static synchronized void stopHeartbeatTaskTimer() {
        synchronized (HeartbeatTaskManager.class) {
            Log.m0d(LOG_TAG, "stopHeartbeatTaskTimer");
            if (GlobalStates.isHeartbeatEnabled()) {
                GlobalStates.setHeartbeatEnabled(false);
                timer.cancel();
                Log.m0d(LOG_TAG, "stopHeartbeatTaskTimer -> task timer is cancelled");
            }
        }
    }
}
