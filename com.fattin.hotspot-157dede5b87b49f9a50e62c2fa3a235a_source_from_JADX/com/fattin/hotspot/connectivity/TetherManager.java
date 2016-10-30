package com.fattin.hotspot.connectivity;

import android.content.Context;
import android.net.ConnectivityManager;
import com.fattin.hotspot.app.MainApplication;
import org.acra.ACRAConstants;

public class TetherManager {
    private static TetherManager manager;
    private ConnectivityManager connectivityManager;
    private Context context;
    private String[] mTetherableWifiRegexs;

    static {
        manager = null;
    }

    public static TetherManager getSingleton() {
        if (manager == null) {
            manager = new TetherManager(MainApplication.getAppContext());
        }
        return manager;
    }

    public TetherManager(Context c) {
        this.mTetherableWifiRegexs = null;
        this.context = c;
        this.connectivityManager = (ConnectivityManager) this.context.getSystemService("connectivity");
    }

    public boolean isMobileDataEnabled() {
        boolean isMobileDataEnabled = false;
        try {
            isMobileDataEnabled = ((Boolean) this.connectivityManager.getClass().getMethod("getMobileDataEnabled", new Class[0]).invoke(this.connectivityManager, new Object[0])).booleanValue();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return isMobileDataEnabled;
    }

    public boolean isTetheringSupported() {
        boolean isSupported = false;
        try {
            isSupported = ((Boolean) this.connectivityManager.getClass().getMethod("isTetheringSupported", new Class[0]).invoke(this.connectivityManager, new Object[0])).booleanValue();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return isSupported;
    }

    public String getTetheredWifiIface() {
        for (String iface : getTetheredIfaces()) {
            if (isWifi(iface)) {
                return iface;
            }
        }
        return ACRAConstants.DEFAULT_STRING_VALUE;
    }

    public String getTetherableWifiIface() {
        for (String iface : getTetherableWifiRegexs()) {
            if (isWifi(iface)) {
                return iface;
            }
        }
        return ACRAConstants.DEFAULT_STRING_VALUE;
    }

    private boolean isWifi(String iface) {
        if (this.mTetherableWifiRegexs == null) {
            this.mTetherableWifiRegexs = getTetherableWifiRegexs();
        }
        for (String regex : this.mTetherableWifiRegexs) {
            if (iface.matches(regex)) {
                return true;
            }
        }
        return false;
    }

    private String[] getTetherableWifiRegexs() {
        String[] wifiRegexs = new String[0];
        try {
            return (String[]) this.connectivityManager.getClass().getMethod("getTetherableWifiRegexs", new Class[0]).invoke(this.connectivityManager, new Object[0]);
        } catch (Exception e) {
            e.printStackTrace();
            return wifiRegexs;
        }
    }

    private String[] getTetheredIfaces() {
        String[] tetheredIfaces = new String[0];
        try {
            return (String[]) this.connectivityManager.getClass().getMethod("getTetheredIfaces", new Class[0]).invoke(this.connectivityManager, new Object[0]);
        } catch (Exception e) {
            e.printStackTrace();
            return tetheredIfaces;
        }
    }
}
