package com.android.myapplication;

import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;

public class WifiAccessManager{
    private static final String SSID = "InternetBnB";
    public static boolean setWifiApState(Context context, boolean enabled) {
        //config = Preconditions.checkNotNull(config);
        try {
            WifiManager mWifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            if (enabled) {
                mWifiManager.setWifiEnabled(false);
            }
            WifiConfiguration conf = getWifiApConfiguration();
            mWifiManager.addNetwork(conf);

            return (Boolean) mWifiManager.getClass().getMethod("setWifiApEnabled", WifiConfiguration.class, boolean.class).invoke(mWifiManager, conf, enabled);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static WifiConfiguration getWifiApConfiguration() {
        WifiConfiguration conf = new WifiConfiguration();
        conf.SSID =  SSID;
        conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
        return conf;
    }
}
