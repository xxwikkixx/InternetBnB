package com.fattin.hotspot.connectivity;

import com.fattin.hotspot.apilib.RestResponseExceptions.APIUserAuthTokenInvalidException;
import com.fattin.hotspot.app.BusinessLogic;
import com.fattin.hotspot.app.GlobalStates;
import com.fattin.hotspot.helpers.Log;
import java.util.TimerTask;

public class HeartbeatTask extends TimerTask implements Runnable {
    private static final String LOG_TAG = "HeartbeatTask";

    public void run() {
        Log.m0d(LOG_TAG, "run");
        if (GlobalStates.isHeartbeatEnabled()) {
            Log.m0d(LOG_TAG, "run -> heartbeatAll");
            try {
                BusinessLogic.heartbeatAll();
            } catch (APIUserAuthTokenInvalidException e) {
                Log.m1e(LOG_TAG, e.getMessage(), e);
            }
        }
    }
}
