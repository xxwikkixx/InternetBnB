package com.fattin.hotspot.apilib;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import com.fattin.hotspot.app.DataProvider;
import com.fattin.hotspot.helpers.Log;
import org.acra.ACRAConstants;

public abstract class APIBroadcastReceiver extends BroadcastReceiver {
    private static final String ACTION_API_USER_UNAUTORIZED = "com.fattin.hotspot.apilib.ACTION_API_USER_UNAUTORIZED";
    private static final String LOG_TAG = "APIBroadcastReceiver";

    public abstract void onAPIUserUnauthorized(Context context, Intent intent);

    public void onReceive(Context context, Intent intent) {
        Log.m3v(LOG_TAG, "onReceive action=" + intent.getAction());
        if (intent.getAction().equals(ACTION_API_USER_UNAUTORIZED)) {
            onAPIUserUnauthorized(context, intent);
        }
    }

    public static void sendBroadcastAPIUserUnauthorized(Context context) {
        Log.m0d(LOG_TAG, "sendBroadcastAPIUserUnauthorized");
        _doLogOut();
        context.sendBroadcast(new Intent(ACTION_API_USER_UNAUTORIZED));
    }

    public static void registerAPIBroadcastReceiver(Context context, APIBroadcastReceiver receiver) {
        Log.m0d(LOG_TAG, "registerAPIBroadcastReceiver");
        context.registerReceiver(receiver, new IntentFilter(ACTION_API_USER_UNAUTORIZED));
    }

    public static void unregisterAPIBroadcastReceiver(Context context, APIBroadcastReceiver receiver) {
        Log.m0d(LOG_TAG, "unregisterAPIBroadcastReceiver");
        context.unregisterReceiver(receiver);
    }

    private static void _doLogOut() {
        Log.m0d(LOG_TAG, "_doLogOut");
        DataProvider.setUserAuthenticationToken(ACRAConstants.DEFAULT_STRING_VALUE);
        DataProvider.setUserLoggedIn(false);
        DataProvider.setHotspotPrice(ACRAConstants.DEFAULT_STRING_VALUE);
        DataProvider.setHotspotAuthToken(ACRAConstants.DEFAULT_STRING_VALUE);
    }
}
