package com.fattin.hotspot.app;

import com.fattin.hotspot.helpers.Installation;
import com.fattin.hotspot.helpers.Log;
import org.acra.ACRA;
import org.acra.ACRAConstants;

public class DataProvider {
    private static final String LOG_TAG = "DataProvider";
    private static InMemoryDatabase inMemoryDatabase;
    private static PersistentStorage persistentStorage;

    static {
        persistentStorage = new PersistentStorage();
        inMemoryDatabase = new InMemoryDatabase();
    }

    public static boolean isUserLoggedIn() {
        Log.m0d(LOG_TAG, "isUserLoggedIn");
        return persistentStorage.getBoolean(Constants.PREF_KEY_USER_LOGGED_IN);
    }

    public static void setUserLoggedIn(boolean is_user_logged_in) {
        Log.m0d(LOG_TAG, "setUserLoggedIn");
        persistentStorage.putBoolean(Constants.PREF_KEY_USER_LOGGED_IN, is_user_logged_in);
    }

    public static String getHotspotPrice() {
        Log.m0d(LOG_TAG, "getHotspotPrice");
        return persistentStorage.getString(Constants.PREF_KEY_HOTSPOT_PRICE);
    }

    public static void setHotspotPrice(String price) {
        Log.m0d(LOG_TAG, "setHotspotPrice");
        persistentStorage.putString(Constants.PREF_KEY_HOTSPOT_PRICE, price);
    }

    public static String getEmail() {
        Log.m0d(LOG_TAG, "getEmail");
        return persistentStorage.getString(Constants.PREF_KEY_USERNAME);
    }

    public static void setEmail(String email) {
        Log.m0d(LOG_TAG, "setEmail");
        persistentStorage.putString(Constants.PREF_KEY_USERNAME, email);
    }

    public static String getUserAuthenticationToken() {
        Log.m0d(LOG_TAG, "getUserAuthToken");
        return persistentStorage.getString(Constants.PREF_KEY_USER_AUTH_TOKEN);
    }

    public static void setUserAuthenticationToken(String token) {
        Log.m0d(LOG_TAG, "setUserAuthenticationToken");
        if (token == null || token.isEmpty()) {
            setUserLoggedIn(false);
        }
        persistentStorage.putString(Constants.PREF_KEY_USER_AUTH_TOKEN, token);
    }

    public static String getHotspotUUID() {
        Log.m0d(LOG_TAG, "getHotspotUUID");
        String hotspot_uuid = persistentStorage.getString(Constants.PREF_KEY_HOTSPOT_UUID);
        if (!hotspot_uuid.isEmpty()) {
            return hotspot_uuid;
        }
        String uuid = Installation.id(MainApplication.getAppContext());
        if (uuid.isEmpty()) {
            return uuid;
        }
        setHotspotUUID(uuid);
        return uuid;
    }

    public static void setHotspotUUID(String uuid) {
        Log.m0d(LOG_TAG, "setHotspotUUID");
        persistentStorage.putString(Constants.PREF_KEY_HOTSPOT_UUID, uuid);
    }

    public static String getHotspotAuthToken() {
        Log.m0d(LOG_TAG, "getHotspotAuthToken");
        return persistentStorage.getString(Constants.PREF_KEY_HOTSPOT_AUTH_TOKEN);
    }

    public static void setHotspotAuthToken(String token) {
        Log.m0d(LOG_TAG, "setHotspotAuthToken");
        persistentStorage.putString(Constants.PREF_KEY_HOTSPOT_AUTH_TOKEN, token);
    }

    public static String getPassword() {
        Log.m0d(LOG_TAG, "getPassword");
        persistentStorage.putString(Constants.PREF_KEY_PASSWORD, ACRAConstants.DEFAULT_STRING_VALUE);
        return inMemoryDatabase.getString(Constants.PREF_KEY_PASSWORD);
    }

    public static void setPassword(String password) {
        Log.m0d(LOG_TAG, "setPassword");
        inMemoryDatabase.set(Constants.PREF_KEY_PASSWORD, password);
    }

    public static boolean getSendCrashReport() {
        Log.m0d(LOG_TAG, "getSendCrashReport");
        return persistentStorage.getBoolean(ACRA.PREF_ENABLE_ACRA);
    }

    public static void setSendCrashReport(boolean allow) {
        Log.m0d(LOG_TAG, "setSendCrashReport");
        persistentStorage.putBoolean(ACRA.PREF_ENABLE_ACRA, allow);
    }

    public static boolean getEulaAgreed() {
        Log.m0d(LOG_TAG, "getEulaAgreed");
        return persistentStorage.getBoolean(Constants.PREF_KEY_AGREED_TO_EULA);
    }

    public static void setEulaAgreed(boolean agreed) {
        Log.m0d(LOG_TAG, "setEulaAgreed");
        persistentStorage.putBoolean(Constants.PREF_KEY_AGREED_TO_EULA, agreed);
    }

    public static boolean getHideDataPlanNotice() {
        Log.m0d(LOG_TAG, "getHideDataPlanNotice");
        return persistentStorage.getBoolean(Constants.PREF_KEY_HIDE_DATAPLAN_NOTICE);
    }

    public static void setHideDataPlanNotice(boolean hide) {
        Log.m0d(LOG_TAG, "setHideDataPlanNotice");
        persistentStorage.putBoolean(Constants.PREF_KEY_HIDE_DATAPLAN_NOTICE, hide);
    }
}
