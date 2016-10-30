package com.fattin.hotspot.connectivity;

import android.content.Context;
import android.content.SharedPreferences.Editor;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Parcel;
import android.preference.PreferenceManager;
import com.fattin.hotspot.app.Constants;
import com.fattin.hotspot.helpers.Log;
import java.lang.reflect.Method;
import org.acra.ACRAConstants;

public class WifiAPManager {
    private static final String LOG_TAG = "WifiAPManager";
    public static final String WIFI_AP_SAVED_CONFIG = "wifi_ap_saved_config";
    public static final String WIFI_AP_STATE_CHANGED_ACTION = "android.net.wifi.WIFI_AP_STATE_CHANGED";
    private static int WIFI_AP_STATE_DISABLED = 0;
    private static int WIFI_AP_STATE_DISABLING = 0;
    private static int WIFI_AP_STATE_ENABLED = 0;
    private static int WIFI_AP_STATE_ENABLING = 0;
    private static int WIFI_AP_STATE_FAILED = 0;
    public static final int WIFI_AP_STATE_UNKNOWN = -1;
    private static WifiAPManager wifiAPManager;
    private final String[] WIFI_STATE_TEXTSTATE;
    private Context context;
    Method getWifiApConfigurationMethod;
    Method getWifiApStateMethod;
    private WifiManager mWifiManager;
    Method setWifiApEnabledMethod;

    static {
        wifiAPManager = null;
    }

    public static WifiAPManager getSingleton() {
        return wifiAPManager;
    }

    public static WifiAPManager getInstance(Context c) {
        if (wifiAPManager == null) {
            wifiAPManager = new WifiAPManager(c);
        }
        return wifiAPManager;
    }

    public WifiAPManager(Context c) {
        this.WIFI_STATE_TEXTSTATE = new String[]{"DISABLING", "DISABLED", "ENABLING", "ENABLED", "FAILED", "5", "6", "7", "8", "9", "DISABLING", "DISABLED", "ENABLING", "ENABLED", "FAILED"};
        this.context = c;
        this.mWifiManager = (WifiManager) this.context.getSystemService("wifi");
        try {
            this.setWifiApEnabledMethod = this.mWifiManager.getClass().getMethod("setWifiApEnabled", new Class[]{WifiConfiguration.class, Boolean.TYPE});
            this.getWifiApStateMethod = this.mWifiManager.getClass().getMethod("getWifiApState", new Class[0]);
            this.getWifiApConfigurationMethod = this.mWifiManager.getClass().getMethod("getWifiApConfiguration", new Class[0]);
            WIFI_AP_STATE_DISABLING = this.mWifiManager.getClass().getField("WIFI_AP_STATE_DISABLING").getInt(this.mWifiManager);
            WIFI_AP_STATE_DISABLED = this.mWifiManager.getClass().getField("WIFI_AP_STATE_DISABLED").getInt(this.mWifiManager);
            WIFI_AP_STATE_ENABLING = this.mWifiManager.getClass().getField("WIFI_AP_STATE_ENABLING").getInt(this.mWifiManager);
            WIFI_AP_STATE_ENABLED = this.mWifiManager.getClass().getField("WIFI_AP_STATE_ENABLED").getInt(this.mWifiManager);
            WIFI_AP_STATE_FAILED = this.mWifiManager.getClass().getField("WIFI_AP_STATE_FAILED").getInt(this.mWifiManager);
        } catch (Exception e) {
            Log.m1e(LOG_TAG, e.getMessage(), e);
        }
    }

    public void startWifiAp() {
        Log.m2i(LOG_TAG, "startWifiAp");
        if (!isWifiApEnablingOrEnabled()) {
            setWifiApEnabled(true);
        }
    }

    public void stopWifiAp() {
        Log.m2i(LOG_TAG, "stopWifiAp");
        if (isWifiApEnablingOrEnabled()) {
            setWifiApEnabled(false);
        }
    }

    public int reconfigWifiAp() {
        Log.m2i(LOG_TAG, "reconfigWifiAp");
        setWifiApEnabled(false);
        return setWifiApEnabled(true);
    }

    public int reconfigWifiAp(WifiConfiguration wifiApConfig) {
        Log.m2i(LOG_TAG, "reconfigWifiAp(WifiConfiguration)");
        setWifiApEnabled(false);
        return setWifiApEnabled(true, wifiApConfig);
    }

    public boolean isWifiApEnablingOrEnabled() {
        return getWifiAPState() == WIFI_AP_STATE_ENABLED || getWifiAPState() == WIFI_AP_STATE_ENABLING;
    }

    public boolean isWifiApEnabled() {
        return getWifiAPState() == WIFI_AP_STATE_ENABLED;
    }

    public boolean isWiFiApValid() {
        return isWiFiApValid(getTargetWifiConfiguration());
    }

    public boolean isWiFiApValid(WifiConfiguration targetWifiApConfig) {
        try {
            if (isWifiApEnablingOrEnabled()) {
                boolean isMatch;
                WifiConfiguration currentWifiApConfig = getCurrentWifiApConfiguration();
                Log.m0d(LOG_TAG, "isWiFiApValid -> Current SSID=" + currentWifiApConfig.SSID);
                Log.m0d(LOG_TAG, "isWiFiApValid -> Current allowedKeyManagement is NONE=" + currentWifiApConfig.allowedKeyManagement.get(0));
                Log.m0d(LOG_TAG, "isWiFiApValid -> Target SSID=" + targetWifiApConfig.SSID);
                Log.m0d(LOG_TAG, "isWiFiApValid -> Target allowedKeyManagement is NONE=" + targetWifiApConfig.allowedKeyManagement.get(0));
                if (targetWifiApConfig.SSID.equals(currentWifiApConfig.SSID) && targetWifiApConfig.allowedKeyManagement.get(0) == currentWifiApConfig.allowedKeyManagement.get(0)) {
                    isMatch = true;
                } else {
                    isMatch = false;
                }
                Log.m0d(LOG_TAG, "isWiFiApValid is " + isMatch);
                return isMatch;
            }
        } catch (Exception e) {
            Log.m1e(LOG_TAG, e.getMessage(), e);
        }
        return false;
    }

    public WifiConfiguration getSavedWifiConfiguration() {
        Log.m2i(LOG_TAG, "getSavedWifiConfiguration");
        String wifiApConfig_str = PreferenceManager.getDefaultSharedPreferences(this.context).getString(WIFI_AP_SAVED_CONFIG, ACRAConstants.DEFAULT_STRING_VALUE);
        if (wifiApConfig_str.isEmpty()) {
            return null;
        }
        byte[] wifiApConfig_byteArray = hexStringToByteArray(wifiApConfig_str);
        Parcel parcel = Parcel.obtain();
        parcel.unmarshall(wifiApConfig_byteArray, 0, wifiApConfig_byteArray.length);
        parcel.setDataPosition(0);
        return (WifiConfiguration) parcel.readValue(WifiConfiguration.class.getClassLoader());
    }

    private WifiConfiguration getTargetWifiConfiguration() {
        Log.m2i(LOG_TAG, "getTargetWifiConfiguration");
        WifiConfiguration wifiConfig = new WifiConfiguration();
        wifiConfig.SSID = Constants.SSID;
        wifiConfig.allowedKeyManagement.set(0);
        return wifiConfig;
    }

    private WifiConfiguration getCurrentWifiApConfiguration() throws NullPointerException {
        Log.m2i(LOG_TAG, "getCurrentWifiApConfiguration");
        WifiConfiguration wifiApConfig = null;
        try {
            wifiApConfig = (WifiConfiguration) this.getWifiApConfigurationMethod.invoke(this.mWifiManager, new Object[0]);
            if (wifiApConfig == null) {
                throw new NullPointerException();
            }
        } catch (Exception e) {
            Log.m1e(LOG_TAG, e.getMessage(), e);
            if (wifiApConfig == null) {
                throw new NullPointerException();
            }
        } catch (Throwable th) {
            if (wifiApConfig == null) {
                NullPointerException nullPointerException = new NullPointerException();
            }
        }
        return wifiApConfig;
    }

    private int setWifiApEnabled(boolean enabled) {
        return setWifiApEnabled(enabled, enabled ? getTargetWifiConfiguration() : null);
    }

    private int setWifiApEnabled(boolean enabled, WifiConfiguration targetWifiConfig) {
        Log.m0d(LOG_TAG, "*** setWifiApEnabled CALLED **** " + enabled);
        if (enabled && this.mWifiManager.getConnectionInfo() != null) {
            this.mWifiManager.setWifiEnabled(false);
        }
        int state = WIFI_AP_STATE_UNKNOWN;
        try {
            this.setWifiApEnabledMethod.invoke(this.mWifiManager, new Object[]{targetWifiConfig, Boolean.valueOf(enabled)});
            state = ((Integer) this.getWifiApStateMethod.invoke(this.mWifiManager, new Object[0])).intValue();
        } catch (Exception e) {
            Log.m1e(LOG_TAG, e.getMessage(), e);
        }
        int loopMax;
        int wifiApState;
        if (!enabled) {
            loopMax = 20;
            wifiApState = getWifiAPState();
            while (loopMax > 0 && (wifiApState == WIFI_AP_STATE_DISABLING || wifiApState == WIFI_AP_STATE_ENABLED || wifiApState == WIFI_AP_STATE_FAILED)) {
                loopMax += WIFI_AP_STATE_UNKNOWN;
                try {
                    Thread.sleep(500);
                } catch (Exception e2) {
                    Log.m1e(LOG_TAG, e2.getMessage(), e2);
                }
                wifiApState = getWifiAPState();
            }
        } else if (enabled) {
            loopMax = 60;
            wifiApState = getWifiAPState();
            while (loopMax > 0 && (wifiApState == WIFI_AP_STATE_ENABLING || wifiApState == WIFI_AP_STATE_DISABLED || wifiApState == WIFI_AP_STATE_FAILED)) {
                loopMax += WIFI_AP_STATE_UNKNOWN;
                try {
                    Thread.sleep(500);
                } catch (Exception e22) {
                    Log.m1e(LOG_TAG, e22.getMessage(), e22);
                }
                wifiApState = getWifiAPState();
            }
        }
        return state;
    }

    public int getWifiAPState() {
        int state = WIFI_AP_STATE_UNKNOWN;
        try {
            state = ((Integer) this.getWifiApStateMethod.invoke(this.mWifiManager, new Object[0])).intValue();
        } catch (Exception e) {
            Log.m1e(LOG_TAG, e.getMessage(), e);
        }
        Log.m0d(LOG_TAG, "getWifiAPState.state " + (state == WIFI_AP_STATE_UNKNOWN ? "UNKNOWN" : this.WIFI_STATE_TEXTSTATE[state]));
        return state;
    }

    public void setSavedWifiApConfig(WifiConfiguration wifiApConfig) {
        Log.m2i(LOG_TAG, "setSavedWifiApConfig");
        Parcel parcel = Parcel.obtain();
        parcel.writeValue(wifiApConfig);
        String wifiApConfig_str = byteArrayToHexString(parcel.marshall());
        Editor editor = PreferenceManager.getDefaultSharedPreferences(this.context).edit();
        editor.putString(WIFI_AP_SAVED_CONFIG, wifiApConfig_str);
        editor.commit();
    }

    public static String byteArrayToHexString(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (int i = 0; i < bytes.length; i++) {
            sb.append(String.format("%02x", new Object[]{Byte.valueOf(bytes[i])}));
        }
        return sb.toString();
    }

    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[(len / 2)];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }
}
